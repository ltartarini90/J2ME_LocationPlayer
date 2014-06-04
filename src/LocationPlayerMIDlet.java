import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


public class LocationPlayerMIDlet extends MIDlet implements CommandListener, PlayerListener {
	
	private Display display;
	private List itemList;
	private Form form;
	private Command stopCommand;
	private Command pauseCommand;
	private Command startCommand;
	private Hashtable items;
	
	public Hashtable getItems() {
		return items;
	}

	public void setItems(Hashtable items) {
		this.items = items;
	}

	private Hashtable itemsInfo;
	private Player player;
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	private StringItem coordinatesString;

	public LocationPlayerMIDlet() {
		display = Display.getDisplay(this);
		
		// Creates an item list to let you select multimedia files to play
		itemList = new List("Select an item to play", List.IMPLICIT);
		// Stop, pause and restart commands
		stopCommand = new Command("Stop", Command.STOP, 1);
		pauseCommand = new Command("Pause", Command.ITEM, 1);
		startCommand = new Command("Start", Command.ITEM, 1);
	
		Retriever retriever = new Retriever(this);
		retriever.start();
		// Form to display when items are being played
		form = new Form("Playing media");
	
		// The form acts as the interface to stop and pause the media
		coordinatesString = new StringItem("Geo Location", "");
		form.append(coordinatesString);
		form.addCommand(stopCommand);
		form.addCommand(pauseCommand);
		form.setCommandListener(this);
	
		// Create a hashtable of items
		items = new Hashtable();	
		// Create a hashtable to hold information about them
		itemsInfo = new Hashtable();
	
		items.put("Siren", "file://siren.wav");
		itemsInfo.put("Siren", "audio/x-wav");
		items.put("Hellobaby", "file://hellobaby.wav");
		itemsInfo.put("Hellobaby", "audio/x-wav");
		items.put("Bond_martini", "file://bond_martini.wav");
		itemsInfo.put("Bond_martini", "audio/x-wav");
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		if(player != null) 
			player.close(); // close the player
	}

	protected void pauseApp() {
		// pause the player
		try {
		  if(player != null) 
			  player.stop();
		} catch(Exception e) {}

	}

	protected void startApp() throws MIDletStateChangeException {
		// When MIDlet is started, use the item list to display elements
		for(Enumeration en = items.keys(); en.hasMoreElements();) {
			itemList.append((String)en.nextElement(), null);
		}

		itemList.setCommandListener(this);

		// show the list when MIDlet is started
		display.setCurrent(itemList);
	}

	public void commandAction(Command command, Displayable displayable) {
		// if list is displayed, the user wants to play the item
		if(displayable instanceof List) {
			List list = ((List) displayable);
			String key = list.getString(list.getSelectedIndex());
			// try and play the selected file
			try {
				playMedia((String) items.get(key), key);
			} catch (Exception e) {
				System.err.println("Unable to play: " + e);
				e.printStackTrace();
			}
		} else if(displayable instanceof Form) {
			// if showing form, means the media is being played and the user is trying to stop or pause the player
			try {
				if(command == stopCommand) { // if stopping the media play
					player.close(); // close the player
					display.setCurrent(itemList); // redisplay the list of media
					form.removeCommand(startCommand); // remove the start command
					form.addCommand(pauseCommand); // add the pause command
				} else if(command == pauseCommand) { // if pausing
					player.stop(); // pauses the media, note that it is called stop
					form.removeCommand(pauseCommand); // remove the pause command
					form.addCommand(startCommand); // add the start (restart) command
				} else if(command == startCommand) { // if restarting
					player.start(); // starts from where the last pause was called
					form.removeCommand(startCommand);
					form.addCommand(pauseCommand);
				}
			} catch(Exception e) {
				System.err.println(e);
			}
		}		
	}
	
    public void playMedia(String locator, String key) throws Exception, Exception {
		// locate the actual file, we are only dealing
		// with file based media here
		String file = locator.substring(locator.indexOf("file://") + 6, locator.length());
		// create the player
		// loading it as a resource and using information about it from the itemsInfo hashtable
		player = Manager.createPlayer(getClass().getResourceAsStream(file), (String)itemsInfo.get(key));
		// a listener to handle player events like starting, closing etc
		player.addPlayerListener(this);
		player.setLoopCount(-1); // play indefinitely
		player.prefetch(); // prefetch
		player.realize(); // realize
		player.start(); // and start		
	}

	public void displayString(String string)
    {
        coordinatesString.setText(string);
    }

	public void playerUpdate(Player player, String event, Object eventData) {
		// if the event is that the player has started, show the form
		// but only if the event data indicates that the event relates to newly
		// stated player, as the STARTED event is fired even if a player is
		// restarted. Note that eventData indicates the time at which the start
		// event is fired.
		if(event.equals(PlayerListener.STARTED) &&  new Long(0L).equals((Long) eventData)) {
			// see if we can show a video control, depending on whether the media
			// is a video or not
			/*VideoControl vc = null;
			if((vc = (VideoControl)player.getControl("VideoControl")) != null) {
				Item videoDisp =
				  (Item)vc.initDisplayMode(vc.USE_GUI_PRIMITIVE, null);
				form.append(videoDisp);
			}*/
			display.setCurrent(form);
		} else if(event.equals(PlayerListener.CLOSED)) {
			form.deleteAll();; // clears the form of any previous controls
			form.append(coordinatesString);
		}		
	}
}
