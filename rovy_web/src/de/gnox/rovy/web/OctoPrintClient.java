package de.gnox.rovy.web;

import java.math.BigDecimal;
import java.net.URI;
import java.text.DecimalFormat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class OctoPrintClient {

	private static final String OctoPrintApiKey = "";

	private static URI OctoPrintUrl = UriBuilder.fromUri("http://localhost:5000").build();

	private Client jerseyClient;

	BigDecimal printerBedTempActual;

	BigDecimal printerBedTempTarget;

	BigDecimal printerHotendTempActual;

	BigDecimal printerHotendTempTarget;

	BigDecimal progressCompletion;

	Integer progressPrintTime;

	Integer progressPrintTimeLeft;

	String jobFileName;

	public void update() {

		printerBedTempActual = null;
		printerBedTempTarget = null;
		printerHotendTempActual = null;
		printerHotendTempTarget = null;

		progressCompletion = null;
		progressPrintTime = null;
		progressPrintTimeLeft = null;
		jobFileName = null;

		try {
			WebResource service = jerseyClient().resource(OctoPrintUrl);
			ClientResponse response = service.path("api").path("printer").queryParam("apikey", OctoPrintApiKey)
					.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
			String entity = response.getEntity(String.class);

			try {
				JSONObject json = new JSONObject(entity);
				printerBedTempActual = json.getJSONObject("temperature").getJSONObject("bed").getBigDecimal("actual");
				printerBedTempTarget = json.getJSONObject("temperature").getJSONObject("bed").getBigDecimal("target");
				printerHotendTempActual = json.getJSONObject("temperature").getJSONObject("tool0")
						.getBigDecimal("actual");
				printerHotendTempTarget = json.getJSONObject("temperature").getJSONObject("tool0")
						.getBigDecimal("target");
			} catch (JSONException ex) {
				ex.printStackTrace();
			}

			response = service.path("api").path("job").queryParam("apikey", OctoPrintApiKey)
					.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
			entity = response.getEntity(String.class);

			try {
				JSONObject json = new JSONObject(entity);
				progressCompletion = json.getJSONObject("progress").getBigDecimal("completion");
				progressPrintTime = json.getJSONObject("progress").getInt("printTime");
				progressPrintTimeLeft = json.getJSONObject("progress").getInt("printTimeLeft");
				jobFileName = json.getJSONObject("job").getJSONObject("file").getString("name");
			} catch (JSONException ex) {
				ex.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		System.out.println(toString());

	}

	public BigDecimal getPrinterBedTempActual() {
		return printerBedTempActual;
	}

	public BigDecimal getPrinterBedTempTarget() {
		return printerBedTempTarget;
	}

	public BigDecimal getPrinterHotendTempActual() {
		return printerHotendTempActual;
	}

	public BigDecimal getPrinterHotendTempTarget() {
		return printerHotendTempTarget;
	}

	public BigDecimal getProgressCompletion() {
		return progressCompletion;
	}

	public Integer getProgressPrintTime() {
		return progressPrintTime;
	}

	public Integer getProgressPrintTimeLeft() {
		return progressPrintTimeLeft;
	}

	public String getJobFileName() {
		return jobFileName;
	}

	@Override
	public String toString() {
		return "printerBedTempActual=" + formatBigDecimal(printerBedTempActual) + 
				"\nprinterBedTempTarget=" + formatBigDecimal(printerBedTempTarget) + 
				"\nprinterHotendTempActual=" + formatBigDecimal(printerHotendTempActual) +
				"\nprinterHotendTempTarget=" + formatBigDecimal(printerHotendTempTarget) +
				"\njobProgressCompletion="+ formatBigDecimal(progressCompletion) + 
				"\njobProgressPrintTime=" + formatTime(progressPrintTime)  + 
				"\njobProgressPrintTimeLeft=" + formatTime(progressPrintTimeLeft)  +
				"\njobFileName=" + jobFileName;
	}
	
	private String formatTime(Integer sek) {
		return sek != null ? (sek / 60) + "" : null;
	}
	
	private String formatBigDecimal(BigDecimal val) {
		return val != null ? val.setScale(2, BigDecimal.ROUND_DOWN).toString() : null;
	}

	private Client jerseyClient() {
		if (jerseyClient == null) {
			ClientConfig config = new DefaultClientConfig();
			jerseyClient = Client.create(config);

		}
		return jerseyClient;
	}

}
