package ca.oson.json;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Provide basic IO support to Oson Java/Json data mapping
 * 
 * @author	David Ruifang He
 * Date	June 15, 2016
 */
public class OsonIO extends Oson {
	static final String DEFAULT_ENCODING = "utf-8";
	
	public OsonIO() {
		super();
	}
	
	
	
	////////////////////////////////////////////
	// serialize
	////////////////////////////////////////////
	
	public void writeValue(PrintWriter writer, Object value) {
		try {
			String text = this.toJson(value);
			writer.write(text);
		} finally {
			try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
	
	public void writeValue(FileWriter writer, Object value) {
		writeValue(new PrintWriter (writer), value);
	}
	
	public void writeValue(Writer writer, Object value) {
		try {
			writer.write(this.toJson(value));
		} catch (IOException e) {
			e.printStackTrace();
			try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
	
	public void writeValue(File file, Object value) {
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new FileWriter(file));
		    		
		    writeValue(writer, value);
		} catch (IOException ex) {
			ex.printStackTrace();
			try {writer.close();} catch (Exception e) {/*ignore*/}
		}
	}
	
	public void writeValue(String file, Object value) {
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), DEFAULT_ENCODING));

		    writeValue(writer, value);
		} catch (IOException ex) {
			ex.printStackTrace();
			try {writer.close();} catch (Exception e) {/*ignore*/}
		}
	}
	
	public void writeValue(OutputStream out, Object value) {
		Writer writer = new BufferedWriter(new OutputStreamWriter(out));
		writeValue(writer, value);
	}
	
	private <T> String print(Consumer c, T value) {
		String str = this.toJson(value);
		c.accept(str);
		return str;
	}
	
	public <T> String print(T value) {
		return print(System.out::println, value);
	}
	
	public <T> String printerr(T value) {
		return print(System.err::println, value);
	}
	
	
	////////////////////////////////////////////
	// deserialize
	////////////////////////////////////////////
	
	public <T> T readValue(String source, Class<T> valueType) {
		return this.fromJson(source, valueType);
	}
	
	public <T> T readValue(byte[] bytes, Class<T> valueType) {
		try {
			String source = new String(bytes, "UTF-8");
			return this.fromJson(source, valueType);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	public <T> T readValue(InputStream is, Class<T> valueType) {
	    try {
	    	Reader reader = new InputStreamReader(is);
	        
	        return readValue(reader, valueType);
	        
	    } finally {
	        try {
	            is.close();
	        } catch (Exception e) {
	            // log error in closing the file
	        }
	    }
	}
	public <T> T readValue(Reader reader, Type valueType) {
		try {
	        StringBuilder contents = new StringBuilder();
	        char[] buffer = new char[4096];

	        int bytesRead = -1;
			while ((bytesRead = reader.read(buffer)) != -1) {
				contents.append(new String(buffer, 0, bytesRead));
			}
			return readValue(contents.toString(), valueType);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	public <T> T readValue(InputStream is, Type type) {
	    try {
			InputStreamReader rdr = new InputStreamReader(is, DEFAULT_ENCODING);

	        StringBuilder contents = new StringBuilder();
	        char[] buffer = new char[4096];

	        int bytesRead = -1;
			while ((bytesRead = rdr.read(buffer)) != -1) {
				contents.append(new String(buffer, 0, bytesRead));
			}
	        
	        return readValue(contents.toString(), type);
	        
	    } catch (IOException e) {
	    	e.printStackTrace();
	    } finally {
	        try {
	            is.close();
	        } catch (Exception e) {
	            // log error in closing the file
	        }
	    }
	    
		return null;
	}
	public <T> T readValue(File file, Type type) {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			
			return readValue(is, type);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	public <T> T readValue(File file, Class<T> valueType) {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			
			return readValue(is, valueType);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public <T> T readValue(String fileName) {
		URL url = getClass().getResource(fileName);
		File file = new File(url.getPath());
		
		return readValue(file, null);
	}
	
	public <T> T readValue(Class<T> valueType, String file) {
		return readValue(new File(file), valueType);
	}
	
	public <T> T readValue(Type type, String file) {
		return readValue(new File(file), type);
	}
	
}
