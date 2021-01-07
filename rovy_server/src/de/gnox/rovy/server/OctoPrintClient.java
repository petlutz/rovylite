package de.gnox.rovy.server;

import java.math.BigDecimal;
import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import de.gnox.rovy.api.RovyTelemetryData;

public class OctoPrintClient {

	private static final String OctoPrintApiKey = "geheim"; // FIXME

	private static URI OctoPrintUrl = UriBuilder.fromUri("http://localhost:5000").build();

	private Client jerseyClient;

	private BigDecimal printerBedTempActual;

	private BigDecimal printerBedTempTarget;

	private BigDecimal printerHotendTempActual;

	private BigDecimal printerHotendTempTarget;

	private BigDecimal progressCompletion;

	private Integer progressPrintTime;

	private Integer progressPrintTimeLeft;

	private String jobFileName;

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
				JSONObject temp = json.getJSONObject("temperature");
				printerBedTempActual = temp.getJSONObject("bed").getBigDecimal("actual");
				printerBedTempTarget = temp.getJSONObject("bed").getBigDecimal("target");
				printerHotendTempActual = temp.getJSONObject("tool0").getBigDecimal("actual");
				printerHotendTempTarget = temp.getJSONObject("tool0").getBigDecimal("target");
			} catch (JSONException ex) {
				System.out.println("OctoPrint: Response not valid: " + entity);
			}
			response = service.path("api").path("job").queryParam("apikey", OctoPrintApiKey)
					.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
			entity = response.getEntity(String.class);
			try {
				JSONObject json = new JSONObject(entity);
				JSONObject progress = json.getJSONObject("progress");
				progressCompletion = progress.isNull("completion") ? null : progress.getBigDecimal("completion");
				progressPrintTime = progress.isNull("printTime") ? null : progress.getInt("printTime");
				progressPrintTimeLeft = progress.isNull("printTimeLeft") ? null : progress.getInt("printTimeLeft");

				JSONObject file = json.getJSONObject("job").getJSONObject("file");
				jobFileName = file.isNull("name") ? null : file.getString("name");

			} catch (JSONException ex) {
				System.out.println("OctoPrint: Response not valid: " + entity);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}
	
	public void cancleJob() {
		String cmd = "{\"command\": \"cancel\"}";
		WebResource service = jerseyClient().resource(OctoPrintUrl);
		service.path("api").path("job").queryParam("apikey", OctoPrintApiKey).header("content-type", MediaType.APPLICATION_JSON)
				.post(String.class, cmd);
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
		return "printerBedTempActual=" + formatBigDecimal(printerBedTempActual) + "\nprinterBedTempTarget="
				+ formatBigDecimal(printerBedTempTarget) + "\nprinterHotendTempActual="
				+ formatBigDecimal(printerHotendTempActual) + "\nprinterHotendTempTarget="
				+ formatBigDecimal(printerHotendTempTarget) + "\njobProgressCompletion="
				+ formatBigDecimal(progressCompletion) + "\njobProgressPrintTime=" + formatTime(progressPrintTime)
				+ "\njobProgressPrintTimeLeft=" + formatTime(progressPrintTimeLeft) + "\njobFileName=" + jobFileName;
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

	public void fillTelemetryData(String prefix, RovyTelemetryData telemetryData) {
		telemetryData.getEntries()
				.add(prefix + "progressCompletion: " + StringUtil.valueWithUnitToString(progressCompletion, "%"));
		telemetryData.getEntries()
				.add(prefix + "progressPrintTime: " + StringUtil.valueWithUnitToString(progressPrintTime, "sec"));
		telemetryData.getEntries()
				.add(prefix + "progressPrintTimeLeft: " + StringUtil.valueWithUnitToString(progressPrintTimeLeft, "sec"));
		telemetryData.getEntries().add(prefix + "bedTempActual: " + StringUtil.valueWithUnitToString(printerBedTempActual, "°"));
		telemetryData.getEntries().add(prefix + "bedTempTarget: " + StringUtil.valueWithUnitToString(printerBedTempTarget, "°"));
		telemetryData.getEntries()
				.add(prefix + "hotendTempActual: " + StringUtil.valueWithUnitToString(printerHotendTempActual, "°"));
		telemetryData.getEntries()
				.add(prefix + "hotendTempTarget: " + StringUtil.valueWithUnitToString(printerHotendTempTarget, "°"));
	}



}
