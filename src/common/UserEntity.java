package common;

/**
 * Represents a user account entry containing credentials and role information.
 */
public class UserEntity extends Entity {

	/**
	 * Column identifiers for user records.
	 */
	public enum UserField { Username, Password, Role }

	/**
	 * Builds a user entity from CSV data.
	 */
	public UserEntity(String csvLine) {
		values = csvLine.split(",", -1);
	}

	/**
	 * Builds a user entity from discrete values.
	 */
	public UserEntity(String username, String password, String role) {
		values = new String[]{username, password, role};
	}

	/**
	 * Retrieves the value of the requested field.
	 */
	public String get(UserField f) {
		return values[f.ordinal()];
	}

	/**
	 * Updates the value of the requested field.
	 */
	public void set(UserField f, String v) {
		values[f.ordinal()] = v;
	}

	/**
	 * Serializes the entity to CSV form.
	 */
	public String toCSV() {
		return super.toCSVFormat();
	}

	@Override
	public String toString() {
		return toCSV();
	}
}
