package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.model.GeoHashUtil;
import com.example.demo.model.Proposal;
import com.example.demo.service.ProposalService;

@SpringBootTest
class TraveloBackendApplicationTests {
	 @Autowired
	    private ProposalService proposalService;
	@Test
	void createDataProposals() throws InterruptedException, ExecutionException {
	    List<Proposal> proposals = new ArrayList<>();
		proposals.add(createProposal("Luxembourg Gardens",48.8462,2.3372,8,20,45,"08:00","20:00"));
		proposals.add(createProposal("Palais Garnier Opera House",48.8719,2.3316,22,45,75,"10:00","17:00"));
		proposals.add(createProposal("Place Vendome Shopping",48.8675,2.3294,30,60,90,"10:00","20:00"));
		proposals.add(createProposal("Galeries Lafayette Rooftop",48.8738,2.3322,12,25,40,"09:30","20:30"));
		proposals.add(createProposal("Rodin Museum",48.8556,2.3155,18,40,75,"10:00","18:00"));
		proposals.add(createProposal("Les Invalides Museum",48.8566,2.3126,20,42,90,"10:00","18:00"));
		proposals.add(createProposal("Petit Palais",48.8660,2.3130,10,22,60,"10:00","18:00"));
		proposals.add(createProposal("Grand Palais Exhibition",48.8661,2.3125,24,48,90,"10:00","19:00"));
		proposals.add(createProposal("Tuileries Garden",48.8635,2.3270,5,15,35,"07:00","21:00"));
		proposals.add(createProposal("Palais Royal Gardens",48.8644,2.3370,7,18,35,"08:00","21:00"));
		proposals.add(createProposal("Latin Quarter Food Tour",48.8495,2.3470,28,55,120,"11:00","20:00"));
		proposals.add(createProposal("Shakespeare and Company Bookstore",48.8526,2.3470,6,18,30,"10:00","22:00"));
		proposals.add(createProposal("Pantheon Visit",48.8462,2.3460,15,34,60,"10:00","18:30"));
		proposals.add(createProposal("Jardin des Plantes",48.8430,2.3593,9,20,50,"08:00","19:00"));
		proposals.add(createProposal("Paris Mosque Tea House",48.8421,2.3555,14,26,45,"09:00","20:00"));
		proposals.add(createProposal("Catacombs of Paris",48.8338,2.3324,27,55,90,"09:45","20:30"));
		proposals.add(createProposal("Montparnasse Tower Observation Deck",48.8422,2.3211,26,52,75,"09:30","23:00"));
		proposals.add(createProposal("Rue Cler Market Tour",48.8558,2.3069,16,30,45,"08:00","18:00"));
		proposals.add(createProposal("Bir Hakeim Bridge Photo Stop",48.8552,2.2875,5,12,20,"00:00","23:59"));
		proposals.add(createProposal("Champ de Mars Picnic",48.8559,2.2986,12,22,45,"08:00","21:00"));
		proposals.add(createProposal("Aquarium de Paris",48.8628,2.2870,24,46,75,"10:00","19:00"));
		proposals.add(createProposal("Bois de Boulogne Bike Rental",48.8625,2.2498,18,35,90,"09:00","18:00"));
		proposals.add(createProposal("Canal Saint-Martin Walk",48.8722,2.3634,6,18,45,"08:00","22:00"));
		proposals.add(createProposal("Place de la Republique Food Market",48.8674,2.3630,20,40,60,"10:00","20:00"));
		proposals.add(createProposal("Le Marais Chocolate Tasting",48.8578,2.3622,22,45,60,"11:00","19:00"));
		proposals.add(createProposal("Place des Vosges",48.8554,2.3656,7,18,35,"08:00","21:00"));
		proposals.add(createProposal("Picasso Museum",48.8599,2.3622,18,38,75,"10:30","18:00"));
		proposals.add(createProposal("Bastille District Walking Tour",48.8532,2.3692,15,30,60,"10:00","19:00"));
		proposals.add(createProposal("Paris Wine Tasting",48.8514,2.3560,40,75,90,"15:00","22:00"));
		proposals.add(createProposal("French Cooking Workshop",48.8505,2.3415,45,80,120,"11:00","18:00"));
		proposals.add(createProposal("Cheese Tasting Experience",48.8548,2.3508,25,50,60,"11:00","20:00"));
		proposals.add(createProposal("Evening Jazz Club",48.8538,2.3475,30,60,120,"18:00","01:00"));
		for (Proposal proposal : proposals) {
		    proposalService.createProposal(proposal);
		}
	}
	private Proposal createProposal(
	        String description,
	        double lat,
	        double lng,
	        double price,
	        double profit,
	        double duration,
	        String open,
	        String close) {

	    Proposal p = new Proposal();

	    // No proposalId -> Firebase UUID will be created
	    p.setTripId("GLOBAL_MARKETPLACE");
	    p.setBusinessId("business-test");

	    p.setDescription(description);

	    p.setLat(lat);
	    p.setLng(lng);
	    p.setLocation(lat + "," + lng);

	    p.setPrice(price);
	    p.setProfit(profit);
	    p.setDurationMinutes(duration);

	    p.setOpenTime(open);
	    p.setCloseTime(close);

	    p.setStatus(Proposal.Status.PENDING);

	    Set<String> hashes =
	            GeoHashUtil.getGeoHashGrid(lat, lng);

	    p.setGeohash(hashes.iterator().next());

	    return p;
	}

}
