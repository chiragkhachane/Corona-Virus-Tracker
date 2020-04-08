package com.chirag.coronavirustracker.services;

import com.chirag.coronavirustracker.models.*;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;



@Service
public class CoronaVirusDataService {

	private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

	private List<LocationStats> allStats = new ArrayList<>();

	@PostConstruct
	@Scheduled(cron = "* * 1 * * *")
	public void fetchVirusData() throws IOException, InterruptedException {
		List<LocationStats> newStats = new ArrayList<>();

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();

		HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
//		System.out.println(httpResponse.body());

		StringReader csvBOdyReader = new StringReader(httpResponse.body());
		Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvBOdyReader);

		for (CSVRecord record : records) {
//			String state = record.get("Province_State");
//			System.out.println(state);
			LocationStats locationStat = new LocationStats();
			locationStat.setState(record.get("Province/State"));
			locationStat.setCountry(record.get("Country/Region"));
			
			int latestCases = Integer.parseInt(record.get(record.size()-1));
			int previousDayCases = Integer.parseInt(record.get(record.size()-2));
			
			locationStat.setDiffFromPrevDay(latestCases - previousDayCases);
			locationStat.setLatestTotalCases(Integer.parseInt(record.get(record.size() - 1)));
//			System.out.println(locationStat);
			newStats.add(locationStat);

		}
		this.allStats = newStats;
	}

	public List<LocationStats> getAllStats() {
		return allStats;
	}


}

