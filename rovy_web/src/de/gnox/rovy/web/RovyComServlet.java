package de.gnox.rovy.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.gnox.rovy.api.RovyCom;
import de.gnox.rovy.api.RovyCommand;
import de.gnox.rovy.api.RovyCommandType;

/**
 * Servlet implementation class RoverComServlet
 */
@WebServlet("/RovyComServlet")
public class RovyComServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RovyComServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			RovyCom rovyCom = null;
			try {
				rovyCom = (RovyCom) Naming.lookup("rmi://127.0.0.1:1234/RovyCom");
			} catch (NotBoundException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			
			String page = request.getParameter("page");
		
			if (page == null)
				page = "com";
			
			if (page.equals("media")) 
				showMediaPage(request, response);
			else if (page.equals("com")) 
				showComPage(request, response, rovyCom);
			else
				response.getWriter().append("unknown page");
			
		} catch (Exception e) {
			// logout
			// request.getSession().setAttribute("sessionId", null);
			e.printStackTrace();
			response.getWriter().append("Exception occured: " + e.getMessage());
			throw e;
		}
	}

	// private void showTelemetryPage(HttpServletRequest request,
	// HttpServletResponse response, Rover rover)
	// throws IOException {
	// writeTelemetryPage(response.getWriter(), rover);
	// }

	// private void showLoginPage(HttpServletRequest request,
	// HttpServletResponse response) throws IOException {
	// PrintWriter w = response.getWriter();
	// w.append("<html><body><center>");
	// w.append("<img src='icon2_64.png'>");
	// w.append("<form action='RovyComServlet'>");
	// w.append("<input type='text' name='pwd'><br>");
	// w.append("<button type='submit'>login</button><br>");
	// w.append("</form>");
	// w.append("</center></body></html>");
	// }
	
	private void showMediaPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		
		
		PrintWriter w = response.getWriter();
		
		
		w.append("<html><body>");
		w.append("<a href='RovyComServlet'><img src='icon2_64.png'></a><br><br>");

		List<String> files = new ArrayList<>(getServletContext().getResourcePaths("/"));
		Collections.sort(files, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}
		});
		
		for (String file : files)
			if (file.startsWith("/rovy"))
				w.append("<a href='" + getServletContext().getContextPath() + file + "'>" + file + "</a><br>");
		
		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='ClearMediaCache'>clear</button>");
		w.append("</form>");
		w.append("</body></html>");
		
	}

	private void showComPage(HttpServletRequest request, HttpServletResponse response, RovyCom rover)
			throws IOException {
		String commandType = request.getParameter("command");
		if (commandType != null) {
			RovyCommand command = new RovyCommand(RovyCommandType.valueOf(commandType),
					new HashMap<>(request.getParameterMap()));
			rover.performCommand(command);
		}
		String showOffButton = request.getParameter("showOffButton");
		writeComPage(response.getWriter(), rover, Objects.equals("true", showOffButton));
	}

	private void writeComPage(PrintWriter w, RovyCom rover, boolean showOffButton) throws RemoteException {
		w.append("<html><head><meta name='viewport' content='width=640' /></head><body>");

		w.append("<table><tr><td valign='top' colspan='3' style='background-color: #abc; padding:8;'>");
		w.append(
				"<center><a href='RovyComServlet'><img src='icon2_64.png'></a><br><strong>. . . R o v y C o m . . .</strong></center>");
		
		w.append("</td></tr><td style=' background-color: #abc; padding:8;'><center><br>");
		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='PowerOn' style='font-size: xx-large;'>ON</button><br><br>");
		if (showOffButton)
			w.append("<button type='submit' name='command' value='PowerOff' style='font-size: xx-large;'>OFF!</button>");
		else
			w.append("<a href='RovyComServlet?showOffButton=true'>OFF?</a>");
		w.append("</form>");
		w.append("<form action='RovyComServlet'>");
		w.append("value:<input size='5' type='text' name='temp' value='25'><br>");
		w.append("<button type='submit' name='command' value='SetTargetTemperatur' style='font-size: xx-large;'>set target temp</button>");
		w.append("</form>");
		
		w.append("</center></td><td style=' background-color: #abc; padding:8;'><center><br>");

		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='CapturePicture' style='font-size: xx-large;'>capture picture</button><br><br>");
		w.append("<button type='submit' name='command' value='CaptureBigPicture' style='font-size: xx-large;'>capture big picture</button>");
		w.append("</form>");
		w.append("<form action='RovyComServlet'>");
		w.append("sec:<input size='5' type='text' name='seconds' value='15'><br>");
		w.append("<button type='submit' name='command' value='CaptureVideo' style='font-size: xx-large;'>capture video</button>");
		w.append("</form>");
		w.append("</center></td><td style=' background-color: #abc; padding:8;'><center><br>");
		w.append("<form action='RovyComServlet'>");
		w.append("<button type='submit' name='command' value='LightOn' style='font-size: xx-large;'>light on</button><br><br>");
		w.append("<button type='submit' name='command' value='LightOff' style='font-size: xx-large;'>light off</button><br><br>");
		w.append("<button type='submit' name='command' value='AbortJob' style='font-size: xx-large;'>abort job</button><br><br>");
		w.append("</form>");
	
		w.append("</center></td></tr><tr><td valign='top' colspan='3' style='width:640; min-width:640;'><center>");
		String camPicture = rover.getCamPicture();
		String camVideo = rover.getCamVideo();
		if (camVideo != null) {
			w.append("<p><video width='640' height='480' controls autoplay>");
			w.append(" <source src='" + camVideo + "' type='video/mp4'>");
			w.append("Your browser does not support the video tag.");
			w.append("</video></p>");
		}
		if (camPicture != null)
			w.append("<p><img src='" + camPicture + "' alt='no image'></p>");


		w.append("</center></td></tr><tr><td valign='top' colspan='3' style='width:640; min-width:640; background-color: #abc; padding:8;'>");
		
		w.append("<p><pre><u>Telemetry Data</u><br>");
		rover.getTelemetryData().getEntries().forEach(msg -> {
			w.append(msg + "<br>");
		});		
		w.append("</pre></p>");
		
		w.append("</td></tr><tr><td valign='top' colspan='3' style='width:640; min-width:640; background-color: #abc; padding:8;'>");
		
//		w.append("<p><pre><u>OctoPrint Data</u><br>");
//		
//		OctoPrintClient op = new OctoPrintClient();
//		op.update();
//		w.append(op.toString());
//		
//		w.append("</pre></p>");
//		
//		w.append("</td></tr><tr><td valign='top' colspan='3' style='width:640; min-width:640; background-color: #abc; padding:8;'>");
//		w.append("<a href='RovyComServlet?page=media'>media</a>");
//
//		w.append("</td></tr></table>");
		// w.append("Telemetry Data: " + telemetryData);
		w.append("</body></html>");

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
