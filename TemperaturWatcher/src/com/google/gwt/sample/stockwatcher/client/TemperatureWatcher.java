package com.google.gwt.sample.stockwatcher.client;

import java.util.ArrayList;
import java.util.Date;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TemperatureWatcher implements EntryPoint {

	private VerticalPanel mainPanel = new VerticalPanel();
	private FlexTable temperatureFlextable = new FlexTable();
	private HorizontalPanel labelPanel = new HorizontalPanel();
	private HorizontalPanel addPanel = new HorizontalPanel();
	private TextBox newSymbolTextBox = new TextBox();
	private TextBox newCityTextBox = new TextBox();
	private TextBox newAreaTextBox = new TextBox();
	private Button addCityButton = new Button("Add");
	private Label lastUpdatedLabel = new Label();
	private ArrayList<Temperature> listOfTemperatures = new ArrayList<Temperature>();
	private TemperatureServiceAsync temperaturesSvc = GWT.create(TemperatureService.class);
	private Label errorMsgLabel = new Label();
	private final static int REFRESH_INTERVAL =5000*10;
	private Temperature currentPlace =null;
	
	
	//The variables below are for the part when the user should be able to add "own" data

	private FlexTable temperatureFlextable2 = new FlexTable();

	FlexTableDragController dragController = new FlexTableDragController(RootPanel.get(), false, this);
	FlexTableDropController dropController = new FlexTableDropController(temperatureFlextable2, this);
	/**
	 * Entry point method.
	 */
	public void onModuleLoad() {
		//Create table for stock data.
		temperatureFlextable.addTableListener(new TableListener(){

			@Override
			@Deprecated
			public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
				setCurrentPlace(temperatureFlextable.getText(row, 0),temperatureFlextable.getText(row, 1), temperatureFlextable.getText(row, 2));
				dragController.dragStart();
				System.out.println("in the on cell clicked");
			}
			
		});
		temperatureFlextable.setText(0, 0, "Country");
		temperatureFlextable.setText(0, 1, "Area");
		temperatureFlextable.setText(0, 2, "City");
		temperatureFlextable.setText(0, 3, "Temp");
		temperatureFlextable.setText(0, 4, "Change");
		temperatureFlextable.setText(0, 5, "Remove");
		temperatureFlextable.setCellPadding(6);
		// Add styles to elements in the stock list table.
		temperatureFlextable.getRowFormatter().addStyleName(0, "watchListHeader");
		temperatureFlextable.addStyleName("watchList");
		//	    stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
		//	    stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
		temperatureFlextable.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");
		temperatureFlextable.getCellFormatter().addStyleName(0, 4, "watchListRemoveColumn");
		newSymbolTextBox.setTitle("Country");
		newAreaTextBox.setTitle("Region");
		newCityTextBox.setTitle("City");
		addPanel.add(newSymbolTextBox);
		addPanel.add(newAreaTextBox);
		addPanel.add(newCityTextBox);

		addPanel.add(addCityButton);
		addPanel.addStyleName("addPanel");
		errorMsgLabel.setStyleName("errorMessage");
		errorMsgLabel.setVisible(false);

		//part 2
		//Create table for stock data.
		temperatureFlextable2.setText(0, 0, "Country");
		temperatureFlextable2.setText(0, 1, "Area");
		temperatureFlextable2.setText(0, 2, "City");
		temperatureFlextable2.setText(0, 3, "Temp");
		temperatureFlextable2.setText(0, 4, "Change");
		temperatureFlextable2.setText(0, 5, "Remove");
		temperatureFlextable2.setCellPadding(6);
				// Add styles to elements in the stock list table.
		temperatureFlextable2.getRowFormatter().addStyleName(0, "watchListHeader");
		temperatureFlextable2.addStyleName("watchList");
				//	    stocksFlexTable.getCellFormatter().addStyleName(0, 1, "watchListNumericColumn");
				//	    stocksFlexTable.getCellFormatter().addStyleName(0, 2, "watchListNumericColumn");
		temperatureFlextable2.getCellFormatter().addStyleName(0, 3, "watchListRemoveColumn");
		temperatureFlextable2.getCellFormatter().addStyleName(0, 4, "watchListRemoveColumn");
	
		
		//DRAG AND DROP
		RootPanel.get().setPixelSize(1000,1000);
		PickupDragController dragController = new PickupDragController(RootPanel.get(),true);
		Image img = new Image("http://code.google.com/webtoolkit/logo-185x175.png");
		RootPanel.get().add(img,40,30);
		dragController.makeDraggable(img);
		//
	
		mainPanel.add(errorMsgLabel);
		// Assemble Main panel.
		mainPanel.add(temperatureFlextable);
		//		mainPanel.add(labelPanel);
		mainPanel.add(addPanel);
		mainPanel.add(temperatureFlextable2);
		mainPanel.add(lastUpdatedLabel);

		// Associate the Main panel with the HTML host page.
		RootPanel.get("stockList").add(mainPanel);
		// Move cursor focus to the input box.
		newSymbolTextBox.setFocus(true);

		// Setup timer to refresh list automatically.
		Timer refreshTimer = new Timer() {
			@Override
			public void run() {
				refreshWatchList();
			}
		};
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);

		// Listen for mouse events on the Add button.
		addCityButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addCity();
			}
		});
		// Listen for keyboard events in the input box.
		newSymbolTextBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					addCity();
				}
			}
		});
		newCityTextBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					addCity();
				}
			}
		});
		newAreaTextBox.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress(KeyPressEvent event) {
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					addCity();
				}
			}
		});

		dragController.setBehaviorDragProxy(true);
		dragController.registerDropController(dropController);
	}

	/**
	 * Add stock to FlexTable. Executed when the user clicks the addStockButton or
	 * presses enter in the newSymbolTextBox.
	 */
	private void addCity() {
		final String country = newSymbolTextBox.getText().toUpperCase().trim();
		final String city = newCityTextBox.getText().toUpperCase().trim();
		final String area = newAreaTextBox.getText().toUpperCase().trim();
		newSymbolTextBox.setFocus(true);

		// Stock code must be between 1 and 10 chars that are numbers, letters, or dots.
		if (!country.matches("^[0-9A-Z������\\.]{1,10}$") || !city.matches("^[0-9A-Z������\\.]{1,10}$") || !area.matches("^[0-9A-Z������\\.]{1,10}$")) {
			Window.alert("Some input is not correct");
			newSymbolTextBox.selectAll();
			return;
		}

		newSymbolTextBox.setText("");
		newAreaTextBox.setText("");
		newCityTextBox.setText("");


		// Don't add the stock if it's already in the table.
		
		for(int i = 0; i<listOfTemperatures.size(); i++){
			if (listOfTemperatures.get(i).getCity().equals(city.toUpperCase())) return;  //can be improved, only compares the city, i.e two cities with the same name in defferent countries/region can't be added
		}

		// Add the stock to the table.
		int row = temperatureFlextable.getRowCount();
		Temperature tempTemp = new Temperature();
		tempTemp.setArea(area);
		tempTemp.setCity(city);
		tempTemp.setCountry(country);
		listOfTemperatures.add(tempTemp);
		temperatureFlextable.setText(row, 0, country);
		temperatureFlextable.setText(row, 1, area);
		temperatureFlextable.setText(row, 2, city);
		temperatureFlextable.setWidget(row, 4, new Label());
		temperatureFlextable.getCellFormatter().addStyleName(row, 3, "watchListNumericColumn");
		temperatureFlextable.getCellFormatter().addStyleName(row, 4, "watchListNumericColumn");
		temperatureFlextable.getCellFormatter().addStyleName(row, 5, "watchListRemoveColumn");
		Widget countryWidget = temperatureFlextable.getWidget(row, 0);
		Widget areaWidget = temperatureFlextable.getWidget(row, 1);
		Widget cityWidget = temperatureFlextable.getWidget(row, 2);
		// Add a button to remove this stock from the table.
		
		
		Button removeStockButton = new Button("x");
		removeStockButton.addStyleDependentName("remove");
		removeStockButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) { //TODO: FIX
				Boolean boo = false;
				int removedIndex = 0;
				for(removedIndex = 0; removedIndex<listOfTemperatures.size() && !boo ; removedIndex++){
					if (listOfTemperatures.get(removedIndex).getCity().toUpperCase().equals(city.toUpperCase())) boo=true;  //can be improved, only compares the city, i.e two cities with the same name in defferent countries/region can't be added
				}
				removedIndex--;
				listOfTemperatures.remove(removedIndex);
				temperatureFlextable.removeRow(removedIndex+1);
			}
		});
		temperatureFlextable.setWidget(row, 5, removeStockButton);

		//gets the stock price

		refreshWatchList();
		
	
		
	}

	private void refreshWatchList() {
		// Initialize the service proxy.
		if (temperaturesSvc == null) {
			temperaturesSvc = GWT.create(TemperatureService.class);

		}

		// Set up the callback object.
		AsyncCallback<ArrayList<Temperature>> callback = new AsyncCallback<ArrayList<Temperature>>() {
			public void onFailure(Throwable caught) {
				  // If the stock code is in the list of delisted codes, display an error message.
		        String details = caught.getMessage();
		        if (caught instanceof DelistedException) {
		          details = "The City '" + ((DelistedException)caught).getSymbol() + "' was delisted";
		        }

		        errorMsgLabel.setText("Error: " + details);
		        errorMsgLabel.setVisible(true);
			}

			@Override
			public void onSuccess(ArrayList<Temperature> result) {
				listOfTemperatures=result;
				updateTable(result);
				
			}
		};

		// Make the call to the stock price service.
		
		temperaturesSvc.getTemperatures(listOfTemperatures, callback); // we need to pares the region and country
		
	}
	

	private void updateTable(ArrayList<Temperature> result) {
		// TODO Auto-generated method stub
		for (int i = 0; i < result.size(); i++) {
			updateTable(result.get(i));
		}
		// Display timestamp showing last refresh.
		lastUpdatedLabel.setText("Last update : "
				+ DateTimeFormat.getMediumDateTimeFormat().format(new Date()));
		  // Clear any errors.
	    errorMsgLabel.setVisible(false);
	}
	/**
	 * Update a single row in the stock table.
	 *
	 * @param price Stock data for a single row.
	 */
	private void updateTable(Temperature temperature) {
		// Make sure the stock is still in the stock table.
		Boolean boo = false;
		int row = 0;
		for(row = 0; row<listOfTemperatures.size() && !boo ; row++){
			if (listOfTemperatures.get(row).getCity().toUpperCase().equals(temperature.getCity().toUpperCase())) boo=true;  //can be improved, only compares the city, i.e two cities with the same name in defferent countries/region can't be added
		}
		if (!boo) return;		
		
		// Format the data in the Price and Change fields.
		String tempText = NumberFormat.getFormat("#,##0.00").format(
				temperature.getTemperature());
		NumberFormat changeFormat = NumberFormat.getFormat("+#,##0.00;-#,##0.00");
		String changeText = changeFormat.format(temperature.getChange());
		String changePercentText = changeFormat.format(temperature.getChangePercent());

		// Populate the Price and Change fields with new data.
		temperatureFlextable.setText(row, 3, tempText);
		Label changeWidget = (Label)temperatureFlextable.getWidget(row, 4);
		changeWidget.setText(changeText + " (" + changePercentText + "%)");
		// Change the color of text in the Change field based on its value.
		String changeStyleName = "noChange";
		if (temperature.getChangePercent() < -0.1f) {
			changeStyleName = "negativeChange";
		}
		else if (temperature.getChangePercent() > 0.1f) {
			changeStyleName = "positiveChange";
		}

		changeWidget.setStyleName(changeStyleName);
	}
	public Temperature getCurrentPlace(){
		return currentPlace;
	}
	
	public void setCurrentPlace(String country, String region, String city){
		currentPlace.setCity(city);
		currentPlace.setCountry(country);
		currentPlace.setArea(region);
	}


}

