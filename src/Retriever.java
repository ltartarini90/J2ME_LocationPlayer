import java.util.Random;

import javax.microedition.location.Coordinates;
import javax.microedition.location.Criteria;
import javax.microedition.location.Location;
import javax.microedition.location.LocationProvider;

public class Retriever extends Thread {
	 
    private LocationPlayerMIDlet midlet;
 
    public Retriever(LocationPlayerMIDlet midlet)
    {
        /**
          * Constructor
          *
          * EFFECTS: Initialise the server and store midlet information
          *
          * @param midlet The main application midlet
          * @param server Forecast Server URL
          *
          */
        this.midlet = midlet;
 
    }
 
    public void run() {
        /**
        * Entry point of the thread
        *
        * EFFECTS: call to connect() method
        */
        try {
        	checkLocation();
        } catch (Exception ex) {
            ex.printStackTrace();
            midlet.displayString(ex.toString());
        }
    }
 
    public void checkLocation() throws Exception
    {
        String string;
        Location location;
        double latitude = 0, longitude = 0, prevLatitude, prevLongitude;
        LocationProvider locationProvider;
        Coordinates coordinates;
        // Set criteria for selecting a location provider:
        // accurate to 500 meters horizontally
        Criteria criteria= new Criteria();
        criteria.setHorizontalAccuracy(500);
        criteria.setVerticalAccuracy(500);
 
        // Get an instance of the provider
        locationProvider= LocationProvider.getInstance(criteria);
 
        while(true) {
        	prevLatitude = latitude;
        	prevLongitude = longitude;
        	// Request the location, setting a one-minute timeout
	        location = locationProvider.getLocation(60);
	        coordinates = location.getQualifiedCoordinates();
	 
	        if(coordinates != null ) {
	          // Use coordinate information
	          latitude = coordinates.getLatitude();
	          longitude = coordinates.getLongitude();
	          string = "\nLatitude : " + latitude + "\nLongitude : " + longitude;
	 
	        } else {
	            string ="Location API failed";
	        }
	        midlet.displayString(string);
	        if(prevLatitude != latitude || prevLongitude != longitude) {
	        	midlet.getPlayer().close();
	        	String key = chooseKey();
	        	midlet.playMedia((String) midlet.getItems().get(key), key);
	        }
        }
    }

	private String chooseKey() {
		Random random = new Random();
		int n = random.nextInt(midlet.getItems().size());
		if(n == 0)
			return "Siren";
		else if(n == 1)
			return "Bond_martini";
		else
			return "Hellobaby";
	}
}
