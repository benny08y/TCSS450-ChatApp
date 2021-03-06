package tcss450.uw.edu.chatapp.contacts;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tcss450.uw.edu.chatapp.R;
import tcss450.uw.edu.chatapp.model.Contacts;
import tcss450.uw.edu.chatapp.utils.GetAsyncTask;
import tcss450.uw.edu.chatapp.utils.SendPostAsyncTask;


/**
 * Chris Kim,
 * SearchContactsFragment that handles searching for contacts
 */
public class SearchContactsFragment extends Fragment { //implements WaitFragment.OnFragmentInteractionListener {
    private final int MAX_LENGTH = 3;
    private final int ZERO = 0;
    private final int ONE = 1;
    private final int TWO = 2;

    private int mColumnCount = 1;
    private RecyclerView recyclerView;
    private View view;
    //private List<Contacts> mContacts;
    private String mEmail;
    private AutoCompleteTextView mAutoCompleteTextView;

    public SearchContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Gets the list of the user's contacts and puts them into an array list for display on another fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //mContacts = new ArrayList<>(Arrays.asList((Contacts[]) getArguments().getSerializable("contacts")));
            mEmail = getArguments().getSerializable("email").toString();
        }
    }

    /**
     * Sets the recycler view on onCreateView
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search_contacts, container, false);
        // Set the adapter
        //if (view instanceof RecyclerView) {
            Context context = view.getContext();
            //recyclerView = (RecyclerView) view;
        mAutoCompleteTextView = view.findViewById(R.id.search_autocomplete_textview);
        recyclerView = (RecyclerView) view.findViewById(R.id.Search_Contacts_List);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
       // }
        Button button = (Button) view.findViewById(R.id.Search_Contacts_Button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(v);
            }
        });
        return view;
    }

    /**
     * Gets the list of members to populate the auto-fill text box
     */
    @Override
    public void onStart() {
        super.onStart();

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_contacts))
                .appendPath(getString(R.string.ep_contacts_all_members))
                .build();
        new GetAsyncTask.Builder(uri.toString())
                .onPostExecute(this::handleGetAllMembers)
                .build().execute();
    }

    /**
     * Gets the list of members from the backend to populate the auto-fill text box
     * @param result if the call to the webservice was successful or not
     */
    private void handleGetAllMembers(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success") && root.getBoolean("success")){
                JSONArray dataArray = root.getJSONArray("data");
                ArrayList<String> emailList = new ArrayList<>();
                for (int i = 0; i < dataArray.length(); i++){
                    JSONObject currObj = dataArray.getJSONObject(i);
                    emailList.add(currObj.getString("email"));
                    emailList.add(currObj.getString("firstname"));
                    emailList.add(currObj.getString("lastname"));
                    emailList.add(currObj.getString("username"));
                }
                String[] emailArray = new String[emailList.size()];
                emailArray = emailList.toArray(emailArray);
                Log.d("SEARCH", "SEARCH" + Arrays.toString(emailArray));
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_dropdown_item_1line,
                                emailArray);
                mAutoCompleteTextView.setAdapter(adapter);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    //private void onButtonClick(View v) {

//        EditText input = view.findViewById(R.id.search_contact_email);
        //String stringInput = mAutoCompleteTextView.getText().toString();
        //String Email = bundle.getString(stringInput);
        //String Email = "cjkim00@gmail.com";
        /*
        if (!stringInput.isEmpty()) {
            try {
                messageJson.put("searchEmail", stringInput);

            } catch (JSONException e) {
                mAutoCompleteTextView.setError("Enter a valid email");
                Log.e("IN_JSON", "didnt put email");
            }
            new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                    //.onPreExecute(this::onWaitFragmentInteractionShow)
                    .onPostExecute(this::handleContactsGetOnPostExecute)
                    .onCancelled(error -> Log.e("SEND_TAG", error))
                    .build().execute();
        } else {
            mAutoCompleteTextView.setError("Enter a valid email");
        }
        */

    //}


    /**
     * Checks to see if the user inputted a valid number of words into the search box
     * @param v the view
     */
    private void onButtonClick(View v) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_contacts))
                .appendPath(getString(R.string.ep_search_contact))
                .build();
        JSONObject messageJson = new JSONObject();
        Bundle bundle = getArguments();
        //EditText input = view.findViewById(R.id.search_contact_email);
        //String stringInput = input.getText().toString();
        String stringInput = mAutoCompleteTextView.getText().toString();
        String[] words = stringInput.split(" ");
        //if the user inputted one or two words
        if (!stringInput.isEmpty() && words.length < MAX_LENGTH) {
            try {
                //if there were two words then send the two words to the endpoint, if there was only one then send just the one
                if (words.length == TWO) {
                    messageJson.put("first", words[ZERO]);
                    messageJson.put("second", words[ONE]);
                } else {
                    messageJson.put("first", words[ZERO]);
                }

            } catch (JSONException e) {
                //mAutoCompleteTextView.setError("Enter a valid email");
                Log.e("IN_JSON", "didnt put email");
            }
            new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                    //.onPreExecute(this::onWaitFragmentInteractionShow)
                    .onPostExecute(this::handleContactsGetOnPostExecute)
                    .onCancelled(error -> Log.e("SEND_TAG", error))
                    .build().execute();
        } else {
            //input.setError("Enter a valid email");
            new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                    //.onPreExecute(this::onWaitFragmentInteractionShow)
                    .onPostExecute(this::handleContactsGetOnPostExecute)
                    .onCancelled(error -> Log.e("SEND_TAG", error))
                    .build().execute();
        }
    }

    /**
     * Gets the results of the search
     * @param result
     */
    private void handleContactsGetOnPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success") && root.getBoolean("success")) {
                Log.d("SearchContactsFragment: ", "Successfully sent");
                JSONArray members = root.getJSONArray("data");
                ArrayList<Contacts> membersArray = new ArrayList<>();
                for (int i = 0; i < members.length(); i++) {
                    JSONObject search = members.getJSONObject(i);
                    membersArray.add(new Contacts.Builder(search.getString("username"),
                            search.getString("email"))
                            .addFirstName(search.getString("firstname"))
                            .addLastName(search.getString("lastname"))
                            .build());
                }

                Contacts[] resultsArray = new Contacts[membersArray.size()];
                //resultsArray = membersArray.toArray(resultsArray);
                List<Contacts> results = membersArray;
                recyclerView.setAdapter(new SearchContactFragmentRecyclerViewAdapter(getActivity(), mEmail, results));

                //onWaitFragmentInteractionHide();
            } else {
                List<Contacts> results = new ArrayList<>();
                recyclerView.setAdapter(new SearchContactFragmentRecyclerViewAdapter(getActivity(), mEmail, results));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR in handleContactsGetOnPostExecute: ", e.getMessage());
            //notify user
            //onWaitFragmentInteractionHide();
        }
    }

//    @Override
//    public void onWaitFragmentInteractionShow() {
//        getActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .add(R.id.content_home_container, new WaitFragment(), "WAIT")
//                .addToBackStack(null)
//                .commit();
//    }
//    @Override
//    public void onWaitFragmentInteractionHide() {
//        getActivity().getSupportFragmentManager()
//                .beginTransaction()
//                .remove(getActivity().getSupportFragmentManager().findFragmentByTag("WAIT"))
//                .commit();
//    }
    private void loadFragment(Fragment frag) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_home_container, frag)
                .addToBackStack(null);
        transaction.commit();
    }


}
