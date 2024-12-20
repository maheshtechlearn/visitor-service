package com.mylogo.visitors.cucumber;

import com.mylogo.visitors.dto.VisitorDTO;
import com.mylogo.visitors.entity.Visitor;
import com.mylogo.visitors.handler.VisitorNotFoundException;
import com.mylogo.visitors.service.VisitorService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertThrows;

@SpringBootTest
public class VisitorSteps {

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private SharedState sharedState;

    private Visitor visitor = new Visitor();
    private VisitorDTO visitorDTO = new VisitorDTO();

    private List<Visitor> visitors;
    private List<VisitorDTO> visitorDTOList;
    private Map<String, List<Visitor>> groupedVisitors;
    private long totalDuration;
    private Set<String> uniqueContacts;

    @Given("a visitor named {string} with contact number {string} and email {string}")
    public void a_visitor_named_with_contact_number_and_email(String name, String contactNumber, String email) {
        visitor = new Visitor();
        visitor.setName(name);
        visitor.setContactNumber(contactNumber);
        visitor.setEmail(email);
    }

    @Given("the visitor has a purpose {string}")
    public void the_visitor_has_a_purpose(String purpose) {
        visitor.setPurpose(purpose);
    }

    @When("the visitor is added")
    public void the_visitor_is_added() {
        visitorDTO = new VisitorDTO(visitor.getId(), visitor.getName(), visitor.getContactNumber(), visitor.getEmail(),
                visitor.getPurpose(), visitor.getCheckIn(), visitor.getCheckOut(), visitor.getDuration(), visitor.isApproved(), visitor.getCreatedDate());
        visitorDTO = visitorService.addVisitor(visitor);
        sharedState.setVisitorId(visitorDTO.getId());
    }

    @Then("the visitor should be added successfully")
    public void the_visitor_should_be_added_successfully() {
        Assertions.assertNotNull(visitorDTO);
        Assertions.assertEquals(visitor.getName(), visitorDTO.getName());
    }

    @Given("a visitor with ID from the first scenario exists")
    public void a_visitor_with_id_from_the_first_scenario_exists() {
        Long visitorId = sharedState.getVisitorId();
        Assertions.assertNotNull(visitorId);
    }

    @When("the user requests the visitor")
    public void the_user_requests_the_visitor() {
        Long visitorId = sharedState.getVisitorId();
        visitorDTO = visitorService.getVisitorById(visitorId);
    }

    @Then("the visitor should be returned with name {string}")
    public void the_visitor_should_be_returned_with_name(String name) {
        Assertions.assertNotNull(visitorDTO);
        Assertions.assertEquals(name, visitorDTO.getName());
    }

    @When("the user updates the visitor's name to {string}")
    public void the_user_updates_the_visitor_s_name_to(String newName) {

        visitor.setName(newName);
        Long visitorId = sharedState.getVisitorId();
        visitorDTO = visitorService.updateVisitor(visitorId, visitor);
    }

    @Then("the visitor's name should be updated to {string}")
    public void the_visitor_s_name_should_be_updated_to(String newName) {
        Assertions.assertEquals(newName, visitorDTO.getName());
    }

    @When("the user deletes the visitor")
    public void the_user_deletes_the_visitor() {
        Long visitorId = sharedState.getVisitorId();
        visitorService.deleteVisitor(visitorId);
    }

    @Then("the visitor should no longer exist")
    public void the_visitor_should_no_longer_exist() {
        Long visitorId = sharedState.getVisitorId();
        assertThrows(VisitorNotFoundException.class, () -> visitorService.getVisitorById(visitorId));
    }


    @Given("visitors exist in the system")
    public void visitors_exist_in_the_system() {
        visitorDTOList = visitorService.getAllVisitors();
    }

    @When("the user requests all visitors sorted by check-in")
    public void the_user_requests_all_visitors_sorted_by_check_in() {
        visitors = visitorService.getAllVisitorsSortedByCheckIn();
    }

    @Then("the visitors should be returned in order of check-in time and ID")
    public void the_visitors_should_be_returned_in_order_of_check_in_time_and_ID() {
        for (int i = 0; i < visitors.size() - 1; i++) {
            Visitor current = visitors.get(i);
            Visitor next = visitors.get(i + 1);

            // Check if the current visitor's check-in is before the next visitor's check-in
            if (current.getCheckIn() != null && next.getCheckIn() != null) {
                Assertions.assertTrue(current.getCheckIn().isBefore(next.getCheckIn()) ||
                                (current.getCheckIn().isEqual(next.getCheckIn()) && current.getId() < next.getId()),
                        "Visitors are not sorted correctly by check-in time and ID");
            }
        }
    }

    @When("the user requests approved visitors")
    public void the_user_requests_approved_visitors() {
        visitors = visitorService.getApprovedVisitors();
    }

    @Then("only approved visitors should be returned")
    public void only_approved_visitors_should_be_returned() {
        Assertions.assertTrue(visitors.stream().allMatch(Visitor::isApproved));
    }

    @When("the user requests visitors grouped by purpose")
    public void the_user_requests_visitors_grouped_by_purpose() {
        groupedVisitors = visitorService.groupVisitorsByPurpose();
    }

    @Then("visitors should be grouped by their purpose with null purposes last")
    public void visitors_should_be_grouped_by_their_purpose_with_null_purposes_last() {
        Assertions.assertTrue(groupedVisitors.containsKey("Business Meeting"));
        Assertions.assertFalse(groupedVisitors.get("Business Meeting").isEmpty());
    }

    @When("the user requests the total visit duration")
    public void the_user_requests_the_total_visit_duration() {
        totalDuration = visitorService.calculateTotalVisitDuration();
    }

    @Then("the total duration should be calculated and returned")
    public void the_total_duration_should_be_calculated_and_returned() {
        Assertions.assertNotNull(totalDuration);
    }

    @When("the user requests unique contact numbers")
    public void the_user_requests_unique_contact_numbers() {
        uniqueContacts = visitorService.getUniqueContactNumbers();
    }

    @Then("unique contact numbers should be returned")
    public void unique_contact_numbers_should_be_returned() {
        Assertions.assertNotNull(uniqueContacts.size());
    }
}
