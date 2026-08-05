package com.example.demo.service;

import com.example.demo.model.GeoHashUtil;
import com.example.demo.model.Proposal;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class ProposalService {

    // DELETED: private final DatabaseReference db = ... (This was causing the crash!)

    // 1. Send an offer
    public String createProposal(Proposal proposal) {
        // MOVED HERE: Get the reference only when we actually need it
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("proposals");

        if (proposal.getProposalId() == null) {
            proposal.setProposalId(UUID.randomUUID().toString());
        }
        if (proposal.getStatus() == null) {
            proposal.setStatus(Proposal.Status.PENDING);
        }
        String geohash = GeoHashUtil.encode(
                proposal.getLat(),
                proposal.getLng()
        );

        proposal.setGeohash(geohash);
        db.child(proposal.getTripId())
          .child(proposal.getProposalId())
          .setValueAsync(proposal);
        
        return proposal.getProposalId();
    }

    // 2. Get all offers for a specific Trip
    public List<Proposal> getProposalsForTrip(String tripId) throws InterruptedException, ExecutionException {
        // MOVED HERE as well
        DatabaseReference db = FirebaseDatabase.getInstance().getReference("proposals");
        
        CompletableFuture<List<Proposal>> future = new CompletableFuture<>();
        
        db.child(tripId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Proposal> proposals = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Proposal p = child.getValue(Proposal.class);
                    if (p != null) proposals.add(p);
                }
                future.complete(proposals);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(error.toException());
            }
        });
        
        return future.get();
    }
}