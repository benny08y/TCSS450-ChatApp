package tcss450.uw.edu.chatapp.contacts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tcss450.uw.edu.chatapp.HomeActivity;
import tcss450.uw.edu.chatapp.R;
import tcss450.uw.edu.chatapp.model.Contacts;
import tcss450.uw.edu.chatapp.utils.SendPostAsyncTask;

/**
 * Aaron Bardsley
 *
 * This fragment is for viewing pending contact requests sent by the user awaiting confirm/reject
 * from the other user.
 */
public class ContactSentRequests extends Fragment {

    private ContactSentRequestsRecyclerViewAdapter mAdapter;
    private View view;
    private String mEmail;
    private RecyclerView recyclerView;
    private int mColumnCount = 1;
    private List<Contacts> mRequests;


    public ContactSentRequests() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEmail = getArguments().getSerializable("email").toString();
            //mRequests = new ArrayList<>(Arrays.asList((Contacts[]) getArguments().getSerializable("requests")));
        }

    }

    /**
     * Aaron Bardsley
     *
     * end point: contacts/contact_request_sent_by_user
     * retrieve all pending contact requests sent by the user to other users.
     */
    private void getRequests() {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(/*getString(R.string.ep_base_url)*/"final-project-450.herokuapp.com")
                .appendPath(/*getString(R.string.ep_contacts)*/"contacts")
                .appendPath("contact_request_sent_by_user")
                .build();
        //Log.d("CONTACTS SENT REQUEST", "uri ep: " + uri.toString());
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("email", mEmail);
            Log.d("ContactSentRequests", "email: " + mEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                //.onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleSentRequestsOnPostExecute)
                .onCancelled(error -> Log.e("SEND_TAG", error))
                .build().execute();
    }

    /**
     * Aaron Bardsley
     *
     * Build a RecycleView to display all pending contact requests
     * sent by the users to other users.
     */
    private void handleSentRequestsOnPostExecute(final String result) {
        //parse JSON
        try {
            JSONObject root = new JSONObject(result);
            if (root.has("success") && root.getBoolean("success")) {
                JSONArray data = root.getJSONArray("data");
                mRequests = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject jsonContacts = data.getJSONObject(i);
                    mRequests.add(new Contacts.Builder(jsonContacts.getString("username"),
                            jsonContacts.getString("email"))
                            .addFirstName(jsonContacts.getString("firstname"))
                            .addLastName(jsonContacts.getString("lastname"))
                            .build());
                    //Log.e("ContactSentRequests: ", "received a sent contact request");
                }

                // Set the adapter
                if (view instanceof RecyclerView) {
                    Context context = view.getContext();
                    recyclerView = (RecyclerView) view;
                    if (mColumnCount <= 1) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    } else {
                        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
                    }
                    mAdapter = new ContactSentRequestsRecyclerViewAdapter(mRequests);
                    recyclerView.setAdapter(mAdapter);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_contact_sent_requests_list, container, false);

        getRequests();

        return view;
    }

}
