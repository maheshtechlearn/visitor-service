package com.mylogo.visitors.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylogo.visitors.dto.VisitorDTO;
import com.mylogo.visitors.repository.VisitorRepository;
import com.mylogo.visitors.handler.DatabaseOperationException;
import com.mylogo.visitors.handler.VisitorNotFoundException;
import com.mylogo.visitors.handler.VisitorRetrievalException;
import com.mylogo.visitors.entity.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class VisitorService {
    private static final Logger logger = LoggerFactory.getLogger(VisitorService.class);
    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private VisitorEventProducer eventProducer;

    @Autowired
    private ObjectMapper objectMapper;

   @Cacheable(value = "visitors")
    public List<VisitorDTO> getAllVisitors() {
        try {
            logger.info("Attempting to retrieve all visitors");
            List<Visitor> visitors=visitorRepository.findAll();
            return convertEntityToDTO(visitors);
        } catch (Exception e) {
            logger.error("Failed to retrieve visitors from the database", e);
            throw new VisitorRetrievalException("Failed to retrieve visitors from the database", e);
        }
    }

    private List<VisitorDTO> convertEntityToDTO(List<Visitor> visitors) {
        List<VisitorDTO> visitorDTOList = new ArrayList<>();
        visitors.forEach(visitor -> {
            VisitorDTO visitorDTO = new VisitorDTO();
            BeanUtils.copyProperties(visitor, visitorDTO);
            visitorDTOList.add(visitorDTO);
        });
        return visitorDTOList;
    }

    @Cacheable(value = "visitor", key = "#id")
    public VisitorDTO getVisitorById(Long id) {

        try {
            logger.info("Attempting to retrieve visitor with ID: {}", id);
            Optional<Visitor> visitor = visitorRepository.findById(id);
            if (visitor.isEmpty()) {
                logger.warn("Visitor with ID {} not found", id);
                throw new VisitorNotFoundException("Visitor with ID " + id + " not found");
            }
            VisitorDTO visitorDTO=new VisitorDTO();
            CompletableFuture.runAsync(() -> eventProducer.produceVisitorEvent("Visitor fetched: " + visitor.get().getId()));
            BeanUtils.copyProperties(visitor.get(), visitorDTO);
            return visitorDTO;
        }catch (VisitorNotFoundException e){
            throw new VisitorNotFoundException("Visitor with ID " + id + " not found");
        }
        catch (Exception e) {
            logger.error("Error retrieving visitor with ID: {}", id, e);
            throw new DatabaseOperationException("Error retrieving visitor with ID " + id, e);
        }
    }

   @CachePut(value = "visitor", key = "#visitor.id")
   public VisitorDTO addVisitor(Visitor visitor) {
       if (visitor == null) {
           logger.warn("Attempted to add a null Visitor object.");
           throw new IllegalArgumentException("Visitor cannot be null");
       }

       Visitor savedVisitor = null;
       try {
           logger.info("Attempting to save a new visitor: {}", visitor);
           savedVisitor = visitorRepository.save(visitor);
           logger.info("Visitor saved successfully with ID: {}", savedVisitor.getId());

           String message = convertVisitorToJson(savedVisitor);
           CompletableFuture.runAsync(()->eventProducer.produceVisitorEvent(message));
           logger.info("Visitor event sent successfully for visitor ID: {}", savedVisitor.getId());

       } catch (Exception e) {
           logger.error("Error saving visitor with details: {}", visitor, e);
           throw new DatabaseOperationException("Error saving visitor", e);
       }
       VisitorDTO visitorDTO=new VisitorDTO();
       BeanUtils.copyProperties(savedVisitor,visitorDTO);
       return visitorDTO;
   }

    @CachePut(value = "visitor", key = "#id", condition = "#result != null")
    public VisitorDTO updateVisitor(Long id, Visitor visitor) {
        try {
            logger.info("Attempting to update visitor with ID: {}", id);
            return visitorRepository.findById(id)
                    .map(existingVisitor -> {
                        visitor.setId(id);
                        Visitor updatedVisitor = visitorRepository.save(visitor);
                        String message = convertVisitorToJson(updatedVisitor);
                        CompletableFuture.runAsync(()->eventProducer.produceVisitorEvent(message));
                        logger.info("Visitor sent update event successfully for visitor ID: {}", updatedVisitor.getId());
                        VisitorDTO visitorDTO=new VisitorDTO();
                        BeanUtils.copyProperties(updatedVisitor,visitorDTO);
                        return visitorDTO;
                    }).orElseThrow(() -> {
                        logger.warn("Visitor with ID {} not found during update", id);
                        return new VisitorNotFoundException("Visitor with ID " + id + " not found");
                    });
        } catch (VisitorNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating visitor with ID: {}", id, e);
            throw new DatabaseOperationException("Error updating visitor with ID " + id, e);
        }
    }

    @CacheEvict(value = "visitor", key = "#id")
    public void deleteVisitor(Long id) {
        try {
            logger.info("Attempting to delete visitor with ID: {}", id);
            visitorRepository.deleteById(id);
            CompletableFuture.runAsync(()->eventProducer.produceVisitorEvent("Visitor Deleted with visitorID: " + id));
            logger.info("Successfully deleted visitor with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting visitor with ID: {}", id, e);
            throw new DatabaseOperationException("Error deleting visitor with ID " + id, e);
        }
    }


    public CompletableFuture<List<Visitor>> fetchAllVisitors() {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching all visitors");
            return visitorRepository.findAll();
        });
    }

    public CompletableFuture<Long> calculateTotalVisitDuration(List<Visitor> visitors) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Calculating total visit duration");
            return visitors.stream().mapToLong(Visitor::getDuration).sum();
        });
    }


    /**
     * Converts a Visitor object to a JSON string.
     *
     * @param visitor the Visitor object to convert
     * @return the JSON string representation of the Visitor object
     */
    private String convertVisitorToJson(Visitor visitor) {
        try {
            return objectMapper.writeValueAsString(visitor);
        } catch (JsonProcessingException e) {
            logger.error("Error converting Visitor object to JSON", e);
            return "{}";
        }
    }


    public List<Visitor> getAllVisitorsSortedByCheckIn() {
        return visitorRepository.findAll().stream()
                .sorted(Comparator.comparing(Visitor::getCheckIn, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Visitor::getId))
                .collect(Collectors.toList());
    }

    // Filter visitors who have been approved
    public List<Visitor> getApprovedVisitors() {
        return visitorRepository.findAll().stream()
                .filter(Visitor::isApproved)
                .collect(Collectors.toList());
    }

    public Map<String, List<Visitor>> groupVisitorsByPurpose() {
        Map<String, List<Visitor>> groupedVisitors = visitorRepository.findAll().stream()
                .collect(Collectors.groupingBy(visitor ->
                                Optional.ofNullable(visitor.getPurpose()).orElse("Unknown"),
                        LinkedHashMap::new, // Maintain insertion order
                        Collectors.toList()
                ));

        // Move the "Unknown" group to the end of the map
        if (groupedVisitors.containsKey("Unknown")) {
            List<Visitor> unknownVisitors = groupedVisitors.remove("Unknown");
            groupedVisitors.put("Unknown", unknownVisitors);
        }

        return groupedVisitors;
    }

    // Calculate total visit duration for all visitors
    public long calculateTotalVisitDuration() {
        return visitorRepository.findAll().stream()
                .mapToLong(Visitor::getDuration)
                .sum();
    }

    // Find unique contact numbers using a Set
    public Set<String> getUniqueContactNumbers() {
        return visitorRepository.findAll().stream()
                .map(Visitor::getContactNumber)
                .collect(Collectors.toSet());
    }
}
