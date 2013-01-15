package com.google.gwt.sample.stockwatcher.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("temperatures")
public interface TemperatureService extends RemoteService {


	ArrayList<Temperature> getTemperatures(ArrayList<Temperature> temperatures) throws DelistedException;
}