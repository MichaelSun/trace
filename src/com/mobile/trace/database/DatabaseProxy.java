package com.mobile.trace.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseProxy {

    private DatabaseHelper mDbHelper;
    public static DatabaseProxy gDatabaseProxy;
    

    public static DatabaseProxy getDBInstance(Context ctx) {
        if (gDatabaseProxy == null) {
            gDatabaseProxy = new DatabaseProxy(ctx);
        }
        return gDatabaseProxy;
    }
    
    private DatabaseProxy(Context ctx) {
        mDbHelper = new DatabaseHelper(ctx);
    }

    public int delete(String tableName, String selection, String[] selectionArgs) {
        if (TextUtils.isEmpty(tableName)) {
            return 0;
        }
        int num = 0;
        try {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            num = db.delete(tableName, selection, selectionArgs);
            // notifyChange(KaixinConstValues.DB_DELETE_VALUE);
        } catch (Exception e) {
            Log.e("KaixinDB", "delete", e);
        }
        return num;
    }

    public long insert(String tableName, ContentValues values) {
        if (values == null || TextUtils.isEmpty(tableName)) {
            return 0;
        }

        try {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            long id = db.insert(tableName, null, values);
            return id;
            // notifyChange(KaixinConstValues.DB_INSERT_VALUE);
        } catch (Exception e) {
            Log.e("KaixinDB", "insert", e);
        }

        return 0;
    }

    public int bulkInsert(String tableName, ContentValues[] values) {
        if (TextUtils.isEmpty(tableName) || values == null) {
            return 0;
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        long id = 0;
        int length = values.length;
        try {
            for (int i = 0; i < length; i++) {
                id = db.insert(tableName, null, values[i]);
            }
            if (id > 0) {
                // notifyChange(KaixinConstValues.DB_INSERT_VALUE);
            }
            db.setTransactionSuccessful();

        } catch (Exception e) {
            Log.e("KaixinDB", "bulkInsert", e);
        } finally {
            db.endTransaction();
        }

        if (id > 0) {
            return length;
        }
        return 0;
    }

    public Cursor query(String tableName, String selection, String[] selectionArgs, String sortOrder) {
        return query(tableName, null, selection, selectionArgs, sortOrder);
    }
    
    public Cursor query(String tableName, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (TextUtils.isEmpty(tableName)) {
            return null;
        }
        try {
            Cursor c = null;
            SQLiteDatabase db = this.mDbHelper.getReadableDatabase();
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(tableName);
            c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

            // notifyChange(KaixinConstValues.DB_QUERY_VALUE);
            return c;
        } catch (Exception e) {
            Log.e("KaixinDB", "query", e);
        }
        return null;
    }

    public int update(String tableName, ContentValues values, String whereClause, String[] whereArgs) {
        if (TextUtils.isEmpty(tableName) || values == null) {
            return 0;
        }
        int num = 0;
        try {
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            num = db.update(tableName, values, whereClause, whereArgs);
            // notifyChange(KaixinConstValues.DB_UPDATE_VALUE);
        } catch (Exception e) {
            Log.e("KaixinDB", "update", e);
        }

        return num;
    }

}
