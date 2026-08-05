package com.example.demo.controller;

import com.example.demo.model.Proposal;
import com.example.demo.service.ProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
public class ProposalController {

    @Autowired
    private ProposalService proposalService;

    @PostMapping
    public String sendProposal(@RequestBody Proposal proposal) {
        return proposalService.createProposal(proposal);
    }

    @GetMapping("/trip/{tripId}")
    public List<Proposal> getByTrip(@PathVariable String tripId) throws Exception {
        return proposalService.getProposalsForTrip(tripId);
    }
}