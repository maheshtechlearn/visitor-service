package com.mylogo.visitors.service;

import com.mylogo.visitors.convertor.VisitorConverter;
import com.mylogo.visitors.dto.VisitorDTO;
import com.mylogo.visitors.entity.Visitor;
import com.mylogo.visitors.handler.DatabaseOperationException;
import com.mylogo.visitors.handler.VisitorNotFoundException;
import com.mylogo.visitors.repository.VisitorRepository;
import com.mylogo.visitors.validator.VisitorValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class VisitorOperationsService {

    private static final Logger logger = LoggerFactory.getLogger(VisitorOperationsService.class);

    private final VisitorRepository visitorRepository;
    private final EventProducer eventProducer;
    private final VisitorConverter visitorConverter;
    private final VisitorValidator visitorValidator;

    public VisitorOperationsService(
            VisitorRepository visitorRepository,
            EventProducer eventProducer,
            VisitorConverter visitorConverter,
            VisitorValidator visitorValidator) {
        this.visitorRepository = visitorRepository;
        this.eventProducer = eventProducer;
        this.visitorConverter = visitorConverter;
        this.visitorValidator = visitorValidator;
    }

    @Cacheable(value = "visitors")
    public List<VisitorDTO> getAllVisitors() {
        try {
            logger.info("Retrieving all visitors");
            List<Visitor> visitors = visitorRepository.findAll();
            return visitorConverter.toDTOList(visitors);
        } catch (Exception e) {
            logger.error("Error retrieving visitors", e);
            throw new DatabaseOperationException("Error retrieving visitors", e);
        }
    }

    @Cacheable(value = "visitor", key = "#id")
    public VisitorDTO getVisitorById(Long id) {
        logger.info("Fetching visitor with ID: {}", id);
        Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new VisitorNotFoundException("Visitor not found with ID: " + id));
        eventProducer.produceVisitorEvent("Visitor fetched: " + visitor.getId());
        return visitorConverter.toDTO(visitor);
    }

    @CachePut(value = "visitor", key = "#result.id")
    public VisitorDTO addVisitor(Visitor visitor) {
        visitorValidator.validate(visitor);
        Visitor savedVisitor = visitorRepository.save(visitor);
        eventProducer.produceVisitorEvent("Visitor added: " + savedVisitor.getId());
        return visitorConverter.toDTO(savedVisitor);
    }

    @CachePut(value = "visitor", key = "#id")
    @Transactional
    public Visitor updateVisitor(Long id, Visitor visitor) {
        visitorValidator.validate(visitor);
        logger.info("Updating visitor with ID: {}", id);

        Visitor existingVisitor = visitorRepository.findById(id)
                .orElseThrow(() -> new VisitorNotFoundException("Visitor not found with ID: " + id));
        visitor.setId(existingVisitor.getId());
        Visitor updatedVisitor = visitorRepository.save(visitor);

        eventProducer.produceVisitorEvent("Visitor updated: " + updatedVisitor.getId());
        return updatedVisitor;
    }

    @CacheEvict(value = "visitor", key = "#id")
    public void deleteVisitor(Long id) {
        logger.info("Deleting visitor with ID: {}", id);
        if (!visitorRepository.existsById(id)) {
            throw new VisitorNotFoundException("Visitor not found with ID: " + id);
        }
        visitorRepository.deleteById(id);
        eventProducer.produceVisitorEvent("Visitor deleted with ID: " + id);
    }
}
