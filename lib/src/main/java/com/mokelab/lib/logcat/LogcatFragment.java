package com.mokelab.lib.logcat;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mokelab.lib.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

/**
 * This fragment shows logcat of current application
 */
public class LogcatFragment extends Fragment {

    private TextView textView;
    private LogThread thread;

    public static LogcatFragment newInstance() {
        LogcatFragment fragment = new LogcatFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.com_mokelab_lib_logcat_fragment, container, false);
        this.textView = root.findViewById(android.R.id.text1);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        this.thread = new LogThread(this.textView);
        this.thread.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.thread != null) {
            this.thread.stopRunning();
            this.thread = null;
        }
    }

    private static class MyTask extends AsyncTask<Void, String, String> {
        private WeakReference<TextView> textViewRef;

        MyTask(TextView textView) {
            this.textViewRef = new WeakReference<>(textView);
        }

        @Override
        protected String doInBackground(Void... voids) {
            InputStream in = null;
            InputStreamReader sr = null;
            BufferedReader br = null;
            try {
                Process p = Runtime.getRuntime().exec("logcat");
                in = p.getInputStream();
                sr = new InputStreamReader(in);
                br = new BufferedReader(sr);

                String line;
                while ((line = br.readLine()) != null) {
                    publishProgress(line + "\n");
                }
                return "Done";
            } catch (IOException e) {
                return "IO Exception " + e.getMessage();
            } finally {
                if (br != null) try { br.close(); } catch (IOException ignore) { }
                if (sr != null) try { sr.close(); } catch (IOException ignore) { }
                if (in != null) try { in.close(); } catch (IOException ignore) { }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values.length == 0) return;

            TextView textView = this.textViewRef.get();
            if (textView == null) return;

            textView.append(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TextView textView = this.textViewRef.get();
            if (textView == null) return;

            textView.setText(s);
        }
    }

    private static class LogThread extends Thread {
        private boolean running = true;
        private Handler handler;
        private WeakReference<TextView> textViewRef;

        LogThread(TextView textView) {
            this.handler = new Handler(Looper.getMainLooper());
            this.textViewRef = new WeakReference<>(textView);
        }

        void stopRunning() {
            this.running = false;
        }

        @Override
        public void run() {
            super.run();
            InputStream in = null;
            InputStreamReader sr = null;
            BufferedReader br = null;
            try {
                Process p = Runtime.getRuntime().exec("logcat -v brief");
                in = p.getInputStream();
                sr = new InputStreamReader(in);
                br = new BufferedReader(sr);

                while (true) {
                    final String line = br.readLine();
                    if (line == null) break;
                    if (!this.running) break;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = textViewRef.get();
                            if (textView == null) return;
                            if (line.startsWith("E/")) {
                                int before = textView.length();
                                textView.append(line);
                                // set color
                                Spannable text = (Spannable) textView.getText();
                                text.setSpan(new ForegroundColorSpan(Color.RED), before, before + line.length(), 0);
                                textView.setText(text);
                            } else {
                                textView.append(line);
                            }
                            textView.append("\n");
                        }
                    });
                }
            } catch (IOException e) {

            } finally {
                if (br != null) try { br.close(); } catch (IOException ignore) { }
                if (sr != null) try { sr.close(); } catch (IOException ignore) { }
                if (in != null) try { in.close(); } catch (IOException ignore) { }
            }
        }
    }
}
