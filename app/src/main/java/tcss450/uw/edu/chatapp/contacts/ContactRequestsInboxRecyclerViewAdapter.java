package tcss450.uw.edu.chatapp.contacts;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import tcss450.uw.edu.chatapp.R;
import tcss450.uw.edu.chatapp.model.Contacts;
import tcss450.uw.edu.chatapp.utils.SendPostAsyncTask;

/**
 * Aaron Bardsley
 *
 * This class is a RecyclerView for pending contact requests sent to the user
 */
public class ContactRequestsInboxRecyclerViewAdapter extends RecyclerView.Adapter<ContactRequestsInboxRecyclerViewAdapter.ViewHolder> {

    private String mEmail;
    private final List<Contacts> mValues;
    private RecyclerView mRecyclerView;

    public ContactRequestsInboxRecyclerViewAdapter(RecyclerView recyclerView,
                                                   String email,
                                                   List<Contacts> items) {
        mValues = items;
        mEmail = email;
        mRecyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact_requests_inbox, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Aaron Bardsley
     *
     * end point: contacts/handle_request
     * response value of 1 for confirm
     */
    private void onConfirmButtonPress(final ViewHolder holder, int position) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath("final-project-450.herokuapp.com")
                .appendPath("contacts")
                .appendPath("handle_request")
                .build();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("senderEmail", mValues.get(position).getEmail());
            messageJson.put("receiverEmail", mEmail);
            messageJson.put("response", 1);
            Log.e("Inbox Recycler: ", "senderEmail: " + mEmail
                    + " receiverEmail: " + mValues.get(position).getEmail());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("Inbox Recycler: ", "reached before confirmed send async");
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                //.onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleConfirmOnPostExecute)
                .onCancelled(error -> Log.e("SEND_TAG", error))
                .build().execute();

        mValues.remove(position);
        mRecyclerView.removeViewAt(position);
        this.notifyItemRemoved(position);
        this.notifyItemRangeChanged(position, mValues.size());
    }

    /**
     * Aaron Bardsley
     *
     * Simple response from server
     */
    private void handleConfirmOnPostExecute(final String result) {
        try {
            Log.e("Inbox Recycler: ", "reached after confirmed send async");
            JSONObject root = new JSONObject(result);
            boolean success = root.getBoolean("success");
            if (success) {
                //Log.d("Inbox Recycler: ", "Successfully confirmed");
            } else {

                Log.e("Inbox Recycler: ", "Failed to confirm");
            }
            //onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR in handleConfirmOnPostExecute: ", e.getMessage());
            //notify user
            //onWaitFragmentInteractionHide();
        }
    }

    /**
     * Aaron Bardsley
     *
     * end point: contacts/handle_request
     * response value of 0 for reject
     */
    private void onRejectButtonPress(final ViewHolder holder, int position) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath("final-project-450.herokuapp.com")
                .appendPath("contacts")
                .appendPath("handle_request")
                .build();
        JSONObject messageJson = new JSONObject();
        try {
            messageJson.put("senderEmail", mEmail);
            messageJson.put("receiverEmail", mValues.get(position).getEmail());
            messageJson.put("response", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(uri.toString(), messageJson)
                //.onPreExecute(this::onWaitFragmentInteractionShow)
                .onPostExecute(this::handleRejectOnPostExecute)
                .onCancelled(error -> Log.e("SEND_TAG", error))
                .build().execute();

        mValues.remove(position);
        mRecyclerView.removeViewAt(position);
        this.notifyItemRemoved(position);
        this.notifyItemRangeChanged(position, mValues.size());
    }

    /**
     * Aaron Bardsley
     *
     * Simple response from server
     */
    private void handleRejectOnPostExecute(final String result) {
        try {
            JSONObject root = new JSONObject(result);
            boolean success = root.getBoolean("success");
            if (success) {
                Log.d("Inbox Recycler: ", "Successfully rejected");
            }
            //onWaitFragmentInteractionHide();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR in handleRejectOnPostExecute: ", e.getMessage());
            //notify user
            //onWaitFragmentInteractionHide();
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.nicknameView.setText(mValues.get(position).getNickname());
        holder.emailView.setText(mValues.get(position).getEmail());

        holder.mConfirmButton.setOnClickListener(e -> {
            onConfirmButtonPress(holder, position);
        });

        holder.mRejectButton.setOnClickListener(e -> {
            onRejectButtonPress(holder, position);
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView nicknameView;
        public final TextView emailView;
        public Contacts mItem;
        public Button mConfirmButton;
        public Button mRejectButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            nicknameView = view.findViewById(R.id.contact_inbox_nickname);
            emailView = view.findViewById(R.id.contact_inbox_email);
            mConfirmButton = view.findViewById(R.id.contact_button_confirm);
            mRejectButton = view.findViewById(R.id.contact_button_reject);
        }
    }
}
