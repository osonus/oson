package ca.oson.json.path;

public enum SELECTOR {
	//?, And, Or, Expose, Ignore
	AND, // and condition: and or &&
	OR, // or logic condition: or or ||
	EXPOSE, // include this in the result
	IGNORE, // ignore this in the result
	NONE // ?, current
}
