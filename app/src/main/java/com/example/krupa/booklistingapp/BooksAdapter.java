package com.example.krupa.booklistingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BooksAdapter extends ArrayAdapter<Books> {
    private ArrayList<Books> mBooksList;

    public BooksAdapter(Context context, ArrayList<Books> booksList) {
        super(context, 0, booksList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Books book = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView tvTitle = (TextView) convertView.findViewById(R.id.book_title);
        tvTitle.setText(book.getTitle());

        TextView tvSubtitle = (TextView) convertView.findViewById(R.id.subtitle);
        tvSubtitle.setText(book.getSubtitle());

        TextView tvPublishedDate = (TextView) convertView.findViewById(R.id.published_date);
        tvPublishedDate.setText(book.getPublishedDate());

        TextView tvAuthors = (TextView) convertView.findViewById(R.id.authors);
        tvAuthors.setText(book.getAuthors());

        return convertView;
    }
}
