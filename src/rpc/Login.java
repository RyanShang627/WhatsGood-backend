package rpc;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class Login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Login() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();

		try {
			// Fetch HTTP session from the request.
			// Session is null when the session doesn't exist
			HttpSession session = request.getSession(false);

			JSONObject obj = new JSONObject();

			if (session != null) {
				String userId = session.getAttribute("user_id").toString();
				// prepare the response body
				obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			} else {
				obj.put("status", "Invalid Session");
				response.setStatus(403); // 403 means "Authorization error" <=> No access to the service
			}
			// write the JSON object to the response
			RpcHelper.writeJSONObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// close the connection
			connection.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Create the database connection
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			// Fetch the JSON object (input) from the body of the HTTP request
			JSONObject input = RpcHelper.readJSONObject(request);

			// Get the userId and password
			String userId = input.getString("user_id");
			String password = input.getString("password");

			JSONObject obj = new JSONObject();

			// write the OK status and user's fullname to response when the user login is
			// successfully verified
			if (connection.verifyLogin(userId, password)) {
				HttpSession session = request.getSession();

				// Store the user_id into the session attribute when login
				session.setAttribute("user_id", userId);
				// set the valid time of the seesion to 10 mins
				session.setMaxInactiveInterval(600);
				obj.put("status", "OK").put("user_id", userId).put("name", connection.getFullname(userId));
			} else {
				obj.put("status", "User Doesn't Exist");
				response.setStatus(401); // 401: Authentication error <=> Login failure (e.g. wrong password)
			}
			RpcHelper.writeJSONObject(response, obj);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
	}

}
