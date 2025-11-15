package common;

import exceptions.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Base controller for authenticated flows ensuring the user exists in {@code users.csv}.
 */
public abstract class UserController extends Controller {

	protected final String userID; // Username
	protected String role;

	/**
	 * Validates that the supplied user identifier exists in the user database and loads the role.
	 */
	public UserController(Router router,Scanner scanner, String userID) throws InvalidUserIDException {
		super(router,scanner);
		this.userID = userID;

	File file = new File(PathResolver.resource("users.csv"));
		boolean found = false;

		if (!file.exists()) {
			throw new InvalidUserIDException("User database not found.");
		}

		try (Scanner reader = new Scanner(file)) {
			while (reader.hasNextLine()) {
				String line = reader.nextLine().trim();
				if (line.isEmpty()) continue;

				String[] parts = line.split(","); // USERNAME,PASSWORD,ROLE
				if (parts.length < 3) continue;

				if (parts[0].equals(userID)) {
					this.role = parts[2].trim();
					found = true;
					break;
				}
			}
		} catch (IOException e) {
			throw new InvalidUserIDException("Error reading users.csv: " + e.getMessage());
		}

		if (!found) {
			throw new InvalidUserIDException("Invalid user ID: " + userID);
		}
	}

	@Override
	public abstract void initialize();

}
