package com.codewithajit.todoapp.interfaces;

public interface RecyclerViewClickListener {
    void onItemClick(int position);

    void onLongItemClick(int position);

    void onEditButtonClick(int position);
    void onDeleteButtonClick(int position);
    void onDoneButtonClick(int position);
}
