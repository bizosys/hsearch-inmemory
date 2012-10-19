package com.bizosys.hsearch.memory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IdSearcherServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		this.doProcess(req, res);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		this.doProcess(req, res);
	}

	private void doProcess(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		res.setContentType("text/html");
		res.setCharacterEncoding(req.getCharacterEncoding());

		/**
		 * Store all the parameters in the Sensor request object
		 */
		Enumeration<String> reqKeys = req.getParameterNames();

		String action = null;
		String query = null;
		while (reqKeys.hasMoreElements()) {
			String key = (String) reqKeys.nextElement();
			String value = req.getParameter(key);
			if ("action".equals(key))
				action = value;
			else if ("query".equals(key))
				query = value;

		}

		/**
		 * Initiate the sensor response, putting the stamp on it and xsl.
		 */
		PrintWriter out = res.getWriter();

		try {
			if ("load".equals(action)) {
				IdSearcher.getInstance().load(query);
				out.append("OK");
			} else if ("search".equals(action)) {
				out.append(IdSearcher.getInstance().find(query));
			}

		} catch (Exception ex) {
			res.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
					"CONTACT_ADMIN");
		} finally {
			out.flush();
			out.close();
		}
	}
}