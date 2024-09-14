package com.mylogo.visitors.visitormgmt.service;

import com.mylogo.visitors.visitormgmt.handler.DatabaseOperationException;
import com.mylogo.visitors.visitormgmt.handler.VisitorNotFoundException;
import com.mylogo.visitors.visitormgmt.handler.VisitorRetrievalException;
import com.mylogo.visitors.visitormgmt.model.Visitor;
import com.mylogo.visitors.visitormgmt.repository.VisitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class VisitorService {
    private static final Logger logger = LoggerFactory.getLogger(VisitorService.class);
    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private VisitorEventProducer eventProducer;

    @Cacheable(value = "visitors")
    public List<Visitor> getAllVisitors() {
        try {
            logger.info("Attempting to retrieve all visitors");
            return visitorRepository.findAll();
        } catch (Exception e) {
            logger.error("Failed to retrieve visitors from the database", e);
            throw new VisitorRetrievalException("Failed to retrieve visitors from the database", e);
        }
    }

    @Cacheable(value = "visitor", key = "#id")
    public Optional<Visitor> getVisitorById(Long id) {
        try {
            logger.info("Attempting to retrieve visitor with ID: {}", id);
            Optional<Visitor> visitor = visitorRepository.findById(id);
            if (visitor.isEmpty()) {
                logger.warn("Visitor with ID {} not found", id);
                throw new VisitorNotFoundException("Visitor with ID " + id + " not found");
            }
            eventProducer.sendVisitorEvent("Visitor fetched: " + visitor.get().getId());
            return visitor;
        } catch (Exception e) {
            logger.error("Error retrieving visitor with ID: {}", id, e);
            throw new DatabaseOperationException("Error retrieving visitor with ID " + id, e);
        }
    }

    @CachePut(value = "visitor", key = "#visitor.id")
    public Visitor addVisitor(Visitor visitor) {
        Visitor savedVisitor = null;
        try {
            logger.info("Attempting to save a new visitor");
            savedVisitor = visitorRepository.save(visitor);
            eventProducer.sendVisitorEvent("Visitor added: " + savedVisitor.getId());
        } catch (Exception e) {
            logger.error("Error saving visitor", e);
            throw new DatabaseOperationException("Error saving visitor", e);
        }
        return savedVisitor;
    }

    @CachePut(value = "visitor", key = "#id", condition = "#result != null")
    public Visitor updateVisitor(Long id, Visitor visitor) {
        try {
            logger.info("Attempting to update visitor with ID: {}", id);
            return visitorRepository.findById(id)
                    .map(existingVisitor -> {
                        visitor.setId(id);
                        Visitor updatedVisitor = visitorRepository.save(visitor);
                        eventProducer.sendVisitorEvent("Visitor updated: " + updatedVisitor.getId());
                        return updatedVisitor;
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
            eventProducer.sendVisitorEvent("Visitor Deleted with visitorID: " + id);
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
}
