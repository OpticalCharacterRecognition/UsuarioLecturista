package com.fourtails.usuariolecturista.jobs;

import android.text.format.Time;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.appspot.ocr_backend.backend.Backend;
import com.appspot.ocr_backend.backend.model.MessagesGetReadings;
import com.appspot.ocr_backend.backend.model.MessagesGetReadingsResponse;
import com.appspot.ocr_backend.backend.model.MessagesReading;
import com.fourtails.usuariolecturista.MainActivity;
import com.fourtails.usuariolecturista.model.ChartReading;
import com.fourtails.usuariolecturista.ottoEvents.BackendObjectsEvent;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.orhanobut.logger.Logger;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.fourtails.usuariolecturista.ottoEvents.BackendObjectsEvent.Status;
import static com.fourtails.usuariolecturista.ottoEvents.BackendObjectsEvent.Type;

/**
 * Get readings async job
 */
public class GetReadingsJob extends Job {

    boolean responseOk = false;
    boolean retry = true;
    String accountNumber;

    public GetReadingsJob(String accountNumber) {
        super(new Params(Priority.MID).requireNetwork().groupBy("get-readings"));
        this.accountNumber = accountNumber;
    }

    @Override
    public void onAdded() {
    }

    /**
     * TODO: ask for a period of time on the backend, we can't just go and get all the readings ever
     *
     * @throws Throwable
     */
    @Override
    public void onRun() throws Throwable {
        // Use a builder to help formulate the API request.
        Backend.Builder builder = new Backend.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null);
        Backend service = builder.build();

        MessagesGetReadings messagesGetReadings = new MessagesGetReadings();
        messagesGetReadings.setAccountNumber(accountNumber);

        MessagesGetReadingsResponse response = service.reading().get(messagesGetReadings).execute();

        if (response.getOk()) {
            List<MessagesReading> readingsArray = response.getReadings();
            eraseReadingsDataFromLocalDB();
            populateDBWithReadings(readingsArray);
            Logger.json(response.toPrettyString());
            responseOk = true;
        } else {
            responseOk = false;
            if (response.getError().contains("no readings found")) {
                Logger.d(response.getError());
                retry = true;
            } else {
                Logger.e(response.getError());
            }
        }
        // Go back to main activity and call the rest of the backend tasks
        MainActivity.bus.post(new BackendObjectsEvent(Type.READING, Status.NORMAL));

    }

    /**
     * Database erase
     * Erases the db so we don't have to check if the reading already exists and don't put duplicates
     */
    private void eraseReadingsDataFromLocalDB() {
        List<ChartReading> tempList = new Select().from(ChartReading.class).execute();
        if (tempList != null && tempList.size() > 0) {
            // we need the time of the last value to help us render the new values differently
            ChartReading lastListValue = tempList.get(tempList.size() - 1);
            MainActivity.oldReadingsLastDateInMillis = lastListValue.timeInMillis;

            ActiveAndroid.beginTransaction();
            try {
                new Delete().from(ChartReading.class).execute();
                ActiveAndroid.setTransactionSuccessful();
            } catch (Exception e) {
                Logger.e(e, "error deleting existing db");
            } finally {
                ActiveAndroid.endTransaction();
            }
        }

    }

    /**
     * Database save
     * This will attempt to save the readings from the backend to the
     *
     * @param readingsArray they array from the backend
     */
    private void populateDBWithReadings(List<MessagesReading> readingsArray) {
        Time time = new Time();

        ActiveAndroid.beginTransaction();
        try {
            // values come unsorted, and we sort them by date
            Collections.sort(readingsArray, new ReadingsCompare());
            for (MessagesReading readings : readingsArray) {
                long timeInMillis = readings.getCreationDate().getValue();
                time.set(timeInMillis);

                ChartReading chartReading = new ChartReading(
                        time.monthDay,
                        time.month,
                        time.year,
                        timeInMillis,
                        readings.getMeasure(),
                        readings.getUrlsafeKey(),
                        readings.getAccountNumber());
                chartReading.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } catch (Exception e) {
            Logger.e(e, "there was an error saving to the database, most likely the data doesn't have" +
                    "the needed fields from the database or they are null");
        } finally {
            ActiveAndroid.endTransaction();
        }
    }

    /**
     * I don't like inner classes but this is too small for its own file, it just compares 2 values
     */
    class ReadingsCompare implements Comparator<MessagesReading> {

        @Override
        public int compare(MessagesReading lhs, MessagesReading rhs) {
            Long value1 = lhs.getCreationDate().getValue();
            Long values2 = rhs.getCreationDate().getValue();
            return value1.compareTo(values2);
        }
    }

    @Override
    protected void onCancel() {
        Logger.d("ReadingsJob canceled");
        MainActivity.bus.post(new BackendObjectsEvent(Type.READING, Status.ERROR));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return retry;
    }
}
