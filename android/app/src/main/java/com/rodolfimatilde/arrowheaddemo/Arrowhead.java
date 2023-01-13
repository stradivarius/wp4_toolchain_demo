package com.rodolfimatilde.arrowheaddemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Arrowhead {
    public static void register_System(Context context, String ipaddress, int port, String system_name) {
        class register_System extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {

                HttpURLConnection urlConnection = null;
                SharedPreferences pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
                try {
                    String arrowhead_address = pref.getString(MainActivity.ARROWHEAD_ADDRESS, "");
                    if (Objects.equals(arrowhead_address, "")){
                        return "error";
                    }
                    String service_registry_address = arrowhead_address + ":8443" + "/serviceregistry/register-system";
                    String query = "{ \"address\": \""+ipaddress+"\", \"port\": "+port+", \"systemName\": \""+system_name+"\" }";
                    URL url = new URL("http://"+service_registry_address);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json") ;
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    out.close();

                    urlConnection.connect();
                    if (urlConnection.getResponseCode() == 201){
                        InputStream inputStream = urlConnection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String response = bufferedReader.readLine();
                        JSONObject service_desc = new JSONObject(response);

                        return service_desc.getString("id");
                    }
                    else{
                        return "error";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return "error";
                } finally {
                    if (urlConnection!=null){
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                SharedPreferences pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                if (!Objects.equals(s, "error")) {
                    editor.putInt(MainActivity.MY_SYSTEM_ID, Integer.parseInt(s));
                    editor.apply();
                    System.out.println("MySystem registered correctly with ID "+s);
                }
                else{
                    System.out.println("MySystem not registered");
                }
            }
        }

        register_System rs = new register_System();
        rs.execute();
    }

    public static void delete_System(Context context, String ipaddress, int port, String system_name) {
        class delete_System extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {

                HttpURLConnection urlConnection = null;
                SharedPreferences pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
                try {
                    String arrowhead_address = pref.getString(MainActivity.ARROWHEAD_ADDRESS, "");
                    if (Objects.equals(arrowhead_address, "")){
                        return "error";
                    }
                    String service_registry_address = arrowhead_address + ":8443" + "/serviceregistry/unregister-system";
                    service_registry_address += "?address="+ ipaddress +"&port="+ port +"&system_name="+ system_name;
                    URL url = new URL("http://"+service_registry_address);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("DELETE");

                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json") ;

                    urlConnection.connect();
                    System.out.println("delete: "+urlConnection.getResponseCode()+" - "+urlConnection);

                    if (urlConnection.getResponseCode() == 200){
                        SharedPreferences.Editor editor = pref.edit();

                        editor.putInt(MainActivity.MY_SYSTEM_ID, 0);
                        editor.apply();
                        System.out.println("delete with success");
                        return "ok";
                    }
                    else{
                        return "error";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return "error";
                } finally {
                    if (urlConnection!=null){
                        urlConnection.disconnect();
                    }
                }
            }
        }

        delete_System ds = new delete_System();
        ds.execute();
    }


    public static void find_Service_SR(Activity _activity, Context context, String nameIntentAction, String name) {
        class find_Service_SR extends AsyncTask<Void, Void, String> {

            private ProgressBar waiting;
            int system_id, service_id;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                TextView load_error = _activity.findViewById(R.id.load_error);
                load_error.setVisibility(View.INVISIBLE);
                waiting = _activity.findViewById(R.id.progressBar_cyclic);
                waiting.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                SharedPreferences pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
                try {
                    String arrowhead_address = pref.getString(MainActivity.ARROWHEAD_ADDRESS, "");
                    if (Objects.equals(arrowhead_address, "")){
                        return "error";
                    }
                    String service_registry_address = arrowhead_address + ":8443" + "/serviceregistry/query";
                    String query = "{\"serviceDefinitionRequirement\": \""+name+"\"}";

                    URL url = new URL("http://"+service_registry_address);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json") ;
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    out.close();

                    urlConnection.connect();
                    if (urlConnection.getResponseCode() == 200){
                        String URL = "";

                        InputStream inputStream = urlConnection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String response = bufferedReader.readLine();
                        JSONObject service_desc = new JSONObject(response);

                        System.out.println("Instances of "+name+" found:" + service_desc.getJSONArray("serviceQueryData").length());

                        JSONArray array = service_desc.getJSONArray("serviceQueryData");

                        for (int i=0; i < array.length(); i++) {
                            JSONObject service = array.getJSONObject(i);
                            String address = service.getJSONObject("provider").getString("address");
                            String port = service.getJSONObject("provider").getString("port");
                            String uri = service.getString("serviceUri");
                            URL = address + ":" + port + "/" + uri;

                            system_id = service.getJSONObject("provider").getInt("id");
                            service_id = service.getJSONObject("serviceDefinition").getInt("id");
                        }

                        if (!URL.equals("")){
                            return URL;
                        }
                        else{
                            return "error";
                        }
                    }
                    else{
                        return "error";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "error";
                } finally {
                    if (urlConnection!=null){
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                waiting.setVisibility(View.INVISIBLE);
                Intent intent = new Intent();
                intent.putExtra("URL", s);
                intent.putExtra("NAME", name);
                intent.putExtra("SYSTEM_ID", system_id);
                intent.putExtra("SERVICE_ID", service_id);
                intent.setAction(nameIntentAction);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                _activity.sendBroadcast(intent);
            }
        }

        find_Service_SR fs = new find_Service_SR();
        fs.execute();
    }

    public static void find_Service_Orchestrator(Activity _activity, Context context, String nameIntentAction, String ipaddress, int port, String system_name, String serviceName) {
        class find_Service_Orchestrator extends AsyncTask<Void, Void, String> {

            private ProgressBar waiting;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                waiting = _activity.findViewById(R.id.progressBar_cyclic);
                waiting.setVisibility(View.VISIBLE);
            }

            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                SharedPreferences pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
                try {
                    if(pref.getInt(MainActivity.MY_SYSTEM_ID, 0)==0){
                        System.out.println("error");
                    }

                    String arrowhead_address = pref.getString(MainActivity.ARROWHEAD_ADDRESS, "");
                    if (Objects.equals(arrowhead_address, "")){
                        return "error";
                    }

                    String orchestrator_address = arrowhead_address + ":8441" + "/orchestrator/orchestration";

                    String query = "{ \"requesterSystem\": { \"address\": \""+ipaddress+"\", \"metadata\":{}, \"port\": "+port+", \"systemName\": \""+system_name+"\" } }";

                    URL url = new URL("http://"+orchestrator_address);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json") ;
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    out.close();

                    urlConnection.connect();
                    System.out.println("not 200"+urlConnection.getResponseCode());
                    if (urlConnection.getResponseCode() == 200){
                        String URL = "";

                        InputStream inputStream = urlConnection.getInputStream();
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String response = bufferedReader.readLine();
                        JSONObject service_desc = new JSONObject(response);

                        System.out.println("Orchestrator instances of "+serviceName+" found:" + service_desc.getJSONArray("response").length());

                        JSONArray array = service_desc.getJSONArray("response");

                        for (int i=0; i < array.length(); i++) {
                            JSONObject service = array.getJSONObject(i);
                            if (service.getString("serviceUri").equals(serviceName)){
                                String endpoint = service.getJSONObject("provider").getString("address");
                                String port = service.getJSONObject("provider").getString("port");
                                String uri = service.getString("serviceUri");
                                URL = endpoint + ":" + port + "/" + uri;
                                System.out.println("Service instances found");
                            }
                        }

                        if (!URL.equals("")){
                            return URL;
                        }
                        else{
                            return "error";
                        }
                    }
                    else{
                        return "error";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "error";
                } finally {
                    if (urlConnection!=null){
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                waiting.setVisibility(View.INVISIBLE);
                Intent intent = new Intent();
                intent.putExtra("URL", s);
                intent.setAction(nameIntentAction);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                _activity.sendBroadcast(intent);
            }
        }

        find_Service_Orchestrator fs = new find_Service_Orchestrator();
        fs.execute();
    }

    public static void save_management_Authorization(Activity _activity, Context context, int consumerSystemId, int providerSystemId, int serviceId, String serviceName){
        class save_management_Authorization extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                SharedPreferences pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
                try {
                    String arrowhead_address = pref.getString(MainActivity.ARROWHEAD_ADDRESS, "");
                    if (Objects.equals(arrowhead_address, "")){
                        return "error";
                    }
                    String authorization_address = arrowhead_address + ":8445" + "/authorization/mgmt/intracloud";
                    String query="{" +
                            "  \"consumerId\": "+consumerSystemId+"," +
                            "  \"interfaceIds\": [2]," +
                            "  \"providerIds\": ["+providerSystemId+"], "+
                            "  \"serviceDefinitionIds\": ["+serviceId+"] "+
                            "}";
                    URL url = new URL("http://"+authorization_address);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json") ;
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    out.close();

                    urlConnection.connect();
                    if (urlConnection.getResponseCode() == 201){
                        return "OK";
                    }
                    else{
                        return "error";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return "error";
                } finally {
                    if (urlConnection!=null){
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (Objects.equals(s, "error") || s==null){
                    new AlertDialog.Builder(_activity)
                            .setTitle("ERROR")
                            .setMessage("Error with update of managementAuthorization - "+serviceName)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    System.out.println("Error with update of managementAuthorization - "+serviceName);
                }
                else{
                    //Toast.makeText(_activity, "managementAuthorization update with success - "+serviceName, Toast.LENGTH_LONG).show();
                    System.out.println("managementAuthorization update with success - "+serviceName);
                }
            }
        }

        save_management_Authorization sma = new save_management_Authorization();
        sma.execute();
    }

    public static void save_management_Orchestrator(Activity _activity, Context context, int consumerSystemId, String providerSystemName, String providerSystemAddress, int providerSystemPort, String serviceName){
        class save_management_Orchestrator extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                SharedPreferences pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
                try {
                    String arrowhead_address = pref.getString(MainActivity.ARROWHEAD_ADDRESS, "");
                    if (Objects.equals(arrowhead_address, "")){
                        return "error";
                    }
                    String orchestrator_address = arrowhead_address + ":8441" + "/orchestrator/mgmt/store";
                    String query="[" +
                            "  {" +
                            "    \"attribute\": {}," +
                            "    \"cloud\": null," +
                            "    \"consumerSystemId\": "+consumerSystemId+"," +
                            "    \"priority\": 1," +
                            "    \"providerSystem\": {" +
                            "        \"systemName\": \""+providerSystemName+"\"," +
                            "        \"address\": \""+providerSystemAddress+"\"," +
                            "        \"port\": "+providerSystemPort+"" +
                            "     }," +
                            "    \"serviceDefinitionName\": \""+serviceName+"\"," +
                            "    \"serviceInterfaceName\": \"HTTP-INSECURE-JSON\"" +
                            "  }" +
                            "]";
                    System.out.println(query);
                    URL url = new URL("http://"+orchestrator_address);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json") ;
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    out.close();

                    urlConnection.connect();
                    if (urlConnection.getResponseCode() == 200){
                        return "OK";
                    }
                    else{
                        return "error";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return "error";
                } finally {
                    if (urlConnection!=null){
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (Objects.equals(s, "error") || s==null){
                    new AlertDialog.Builder(_activity)
                            .setTitle("ERROR")
                            .setMessage("Error with update of managementOrchestrator - "+serviceName)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    System.out.println("Error with update of managementOrchestrator - "+serviceName);
                }
                else{
                    //Toast.makeText(_activity, "managementOrchestrator update with success - "+serviceName, Toast.LENGTH_LONG).show();
                    System.out.println("managementOrchestrator update with success - "+serviceName);
                }
            }
        }

        save_management_Orchestrator smo = new save_management_Orchestrator();
        smo.execute();
    }

    public static void save_management_Choreographer(Activity _activity, Context context, String nameIntentAction, int repetitions, String serviceNameStartSensor, String serviceNameDatabase, String serviceNameStopSensor){
        class save_management_Choreographer extends AsyncTask<Void, Void, String> {
            @Override
            protected String doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                SharedPreferences pref = context.getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_PRIVATE);
                int max = 1000;
                int min = 1;
                int rn = (int)(Math.random()*(max-min+1)+min);
                String name = "Choreographer-Demo"+rn;
                try {
                    String arrowhead_address = pref.getString(MainActivity.ARROWHEAD_ADDRESS, "");
                    if (Objects.equals(arrowhead_address, "")){
                        return "error";
                    }
                    String choreographer_address = arrowhead_address + ":8457" + "/choreographer/mgmt/plan";
                    String query="{" +
                            "  \"actions\": [" +
                            "    {" +
                            "      \"firstStepNames\": [" +
                            "        \"start\"" +
                            "      ]," +
                            "      \"name\": \"startSensor\"," +
                            "      \"nextActionName\": \"saveData\"," +
                            "      \"steps\": [" +
                            "        {" +
                            "          \"metadata\": null," +
                            "          \"name\": \"start\"," +
                            "          \"nextStepNames\": []," +
                            "          \"parameters\": null," +
                            "          \"quantity\": 1," +
                            "          \"serviceName\": \""+serviceNameStartSensor+"\"" +
                            "        }" +
                            "      ]" +
                            "    }," +
                            "    {" +
                            "      \"firstStepNames\": [" +
                            "        \"fill1\"" +
                            "      ]," +
                            "      \"name\": \"saveData\"," +
                            "      \"nextActionName\": \"stopSensor\"," +
                            "      \"steps\": [";
                    for (int i = 0; i < (repetitions-1); i++) {
                        query +=
                                "        {" +
                                "          \"metadata\": null," +
                                "          \"name\": \"fill" + (i+1) + "\"," +
                                "          \"nextStepNames\": [ \"fill" + (i+2) + "\" ]," +
                                "          \"parameters\": null," +
                                "          \"quantity\": 1," +
                                "          \"serviceName\": \"" + serviceNameDatabase + "\"" +
                                "        },";
                    }
                    query +=
                            "        {" +
                            "          \"metadata\": null," +
                            "          \"name\": \"fill" + repetitions + "\"," +
                            "          \"nextStepNames\": []," +
                            "          \"parameters\": null," +
                            "          \"quantity\": 1," +
                            "          \"serviceName\": \"" + serviceNameDatabase + "\"" +
                            "        }"+
                            "      ]" +
                            "    }," +
                            "    {" +
                            "      \"firstStepNames\": [" +
                            "        \"stop\"" +
                            "      ]," +
                            "      \"name\": \"stopSensor\"," +
                            "      \"nextActionName\": null," +
                            "      \"steps\": [" +
                            "        {" +
                            "          \"metadata\": null," +
                            "          \"name\": \"stop\"," +
                            "          \"nextStepNames\": []," +
                            "          \"parameters\": null," +
                            "          \"quantity\": 1," +
                            "          \"serviceName\": \"" + serviceNameStopSensor + "\"" +
                            "        }" +
                            "      ]" +
                            "    }" +
                            "  ]," +
                            "  \"firstActionName\": \"startSensor\"," +
                            "  \"name\": \"" + name + "\"" +
                            "}";

                    System.out.println(query);
                    URL url = new URL("http://"+choreographer_address);

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    urlConnection.setRequestProperty("Accept", "application/json") ;
                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    BufferedOutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                    writer.write(query);
                    writer.flush();
                    writer.close();
                    out.close();

                    urlConnection.connect();
                    if (urlConnection.getResponseCode() == 201){
                        System.out.println("Saved");
                        urlConnection.disconnect();

                        choreographer_address = arrowhead_address + ":8457" + "/choreographer/mgmt/plan?direction=ASC&sort_field=id";
                        url = new URL("http://"+choreographer_address);

                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();
                        System.out.println("saveManagementChoreographer: "+urlConnection.getResponseCode()+" - "+urlConnection);
                        if (urlConnection.getResponseCode() == 200){
                            String URL = "";

                            InputStream inputStream = urlConnection.getInputStream();
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String response = bufferedReader.readLine();
                            JSONArray array = new JSONArray(response);
                            System.out.println(array);

                            for (int i=0; i < array.length(); i++) {
                                JSONObject service = array.getJSONObject(i);
                                if (service.getString("name").equals(name)) {
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putInt(MainActivity.CHOREOGRAPHER_PLAN_ID, service.getInt("id"));
                                    editor.apply();
                                    return "ok";
                                }
                            }
                            return "error";
                        }
                        else{
                            return "error";
                        }
                    }
                    else{
                        return "error";
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return "error";
                } finally {
                    if (urlConnection!=null){
                        urlConnection.disconnect();
                    }
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                Intent intent = new Intent();
                intent.putExtra("result", s);
                intent.setAction(nameIntentAction);
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                _activity.sendBroadcast(intent);
            }
        }

        save_management_Choreographer smc = new save_management_Choreographer();
        smc.execute();
    }
}