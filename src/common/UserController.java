package common;

import exceptions.*;
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
	public UserController(Router router, Scanner scanner, EntityStore entityStore, String userID) throws InvalidUserIDException {
		super(router, scanner, entityStore);
		this.userID = userID;

		Entity entity = entityStore.findById(PathResolver.resource("users.csv"), userID, "User");
		if (!(entity instanceof UserEntity user)) {
			throw new InvalidUserIDException("Invalid user ID: " + userID);
		}
		this.role = user.get(UserEntity.UserField.Role);
	}

	@Override
	public abstract void initialize();

}
