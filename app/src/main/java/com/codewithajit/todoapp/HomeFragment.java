
package com.codewithajit.todoapp;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codewithajit.todoapp.Adapters.TodoListAdapter;
import com.codewithajit.todoapp.UtilsService.SharedPreferenceClass;
import com.codewithajit.todoapp.interfaces.RecyclerViewClickListener;
import com.codewithajit.todoapp.model.TodoModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements RecyclerViewClickListener {
    FloatingActionButton floatingActionButton;
    SharedPreferenceClass sharedPreferenceClass;
    String token;
    TodoListAdapter todoListAdapter;
    RecyclerView recyclerView;
    TextView empty_tv;
    ProgressBar progressBar;
    ArrayList<TodoModel> arrayList;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferenceClass = new SharedPreferenceClass(getContext());
        token = sharedPreferenceClass.getValue_string("token");

        floatingActionButton = view.findViewById(R.id.add_task_btn);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        recyclerView = view.findViewById(R.id.recycler_view);
        empty_tv = view.findViewById(R.id.empty_tv);
        progressBar = view.findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);


        getTasks();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        return view;
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            showDeleteDialog(arrayList.get(position).getId(), position);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };


    public void showAlertDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.custom_dialog_layout, null);

        final EditText title_field = alertLayout.findViewById(R.id.title);
        final EditText description_field = alertLayout.findViewById(R.id.description);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(alertLayout)
                .setTitle("Add Task")
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInter) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = title_field.getText().toString();
                        String description = description_field.getText().toString();
                        if(!TextUtils.isEmpty(title)) {
                            addTask(title, description);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Please enter title...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        dialog.show();
    }
    public void showUpdateDialog(final  String  id, String title, String description)  {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.custom_dialog_layout, null);

        final EditText title_field = alertLayout.findViewById(R.id.title);
        final EditText description_field = alertLayout.findViewById(R.id.description);

        title_field.setText(title);
        description_field.setText(description);

        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setView(alertLayout)
                .setTitle("Update Task")
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String title = title_field.getText().toString();
                        String description = description_field.getText().toString();

                        updateTask(id, title, description);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }
    public void showDeleteDialog(final String id, final  int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Are you want to delete the task ?")
                .setPositiveButton("Yes", null)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getTasks();
                    }
                })
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteTodo(id, position);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }
    public void showFinishedTaskDialog(final String id, final int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Move to finished task?")
                .setPositiveButton("Yes", null)
                .setNegativeButton("No", null)
                .create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog)alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateToFinishTodo(id, position);
                        alertDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }




    // Get all Todo Task methood
    public void getTasks() {
        arrayList = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://todoappyt.herokuapp.com/api/todo";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        JSONArray jsonArray = response.getJSONArray("todos");

                        if(jsonArray.length() == 0) {
                            empty_tv.setVisibility(View.VISIBLE);
                        } else {
                            empty_tv.setVisibility(View.GONE);
                            for(int i = 0; i < jsonArray.length(); i ++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                TodoModel todoModel = new TodoModel(
                                        jsonObject.getString("_id"),
                                        jsonObject.getString("title"),
                                        jsonObject.getString("description")
                                );
                                arrayList.add(todoModel);
                            }

                            todoListAdapter = new TodoListAdapter(getActivity(), arrayList, HomeFragment.this);
                            recyclerView.setAdapter(todoListAdapter);
                        }

                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


                NetworkResponse response = error.networkResponse;

                if (error == null || error.networkResponse == null) {
                    return;
                }

                String body;

//                final String statusCode = String.valueOf(error.networkResponse.statusCode);

                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    JSONObject errorObject = new JSONObject(body);


                    if(errorObject.getString("msg").equals("Token not valid")) {
                        sharedPreferenceClass.clear();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        Toast.makeText(getActivity(), "Session expired", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getActivity(), errorObject.getString("msg") , Toast.LENGTH_SHORT).show();
                } catch (UnsupportedEncodingException | JSONException e) {
                    // exception
                }


                progressBar.setVisibility(View.GONE);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }



    // Delete Todo Method
    private void deleteTodo(final String id, final  int position) {
        String url = "https://todoappyt.herokuapp.com/api/todo/"+id;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, url, null
                , new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                        arrayList.remove(position);
                        todoListAdapter.notifyItemRemoved(position);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }
    // Add Todo Task Method
    private void addTask(String title, String description) {
        String url = " https://todoappyt.herokuapp.com/api/todo";

        HashMap<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, new JSONObject(body), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if(response.getBoolean("success")) {
                        Toast.makeText(getActivity(), "Added Successfully", Toast.LENGTH_SHORT).show();
                        getTasks();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse response = error.networkResponse;
                if(error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers,  "utf-8"));
                        JSONObject obj = new JSONObject(res);
                        Toast.makeText(getActivity(), obj.getString("msg"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException | UnsupportedEncodingException je) {
                        je.printStackTrace();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }
        };

        // set retry policy
        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(policy);

        // request add
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }
    // Update Todo Task Method
    private  void  updateTask(String id, String title, String description) {
        String url = "https://todoappyt.herokuapp.com/api/todo/"+id;
        HashMap<String, String> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(body),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")) {
                                getTasks();
                                Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", token);
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }
    // Update to finished task
    private void updateToFinishTodo(String id,final int position) {
        String url = "https://todoappyt.herokuapp.com/api/todo/"+id;
        HashMap<String, String> body = new HashMap<>();
        body.put("finished", "true");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, new JSONObject(body),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getBoolean("success")) {
                                arrayList.remove(position);
                                getTasks();

                                todoListAdapter.notifyItemRemoved(position);
                                Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getActivity(), "Position "+ position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLongItemClick(int position) {
        showUpdateDialog(arrayList.get(position).getId(), arrayList.get(position).getTitle(), arrayList.get(position).getDescription());
        Toast.makeText(getActivity(), "Position "+ position, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onEditButtonClick(int position) {
        showUpdateDialog(arrayList.get(position).getId(), arrayList.get(position).getTitle(), arrayList.get(position).getDescription());
    }

    @Override
    public void onDeleteButtonClick(int position) {
        showDeleteDialog(arrayList.get(position).getId(), position);

    }

    @Override
    public void onDoneButtonClick(int position) {
        showFinishedTaskDialog(arrayList.get(position).getId(), position);
        Toast.makeText(getActivity(), "Position "+ position, Toast.LENGTH_SHORT).show();
    }
}

