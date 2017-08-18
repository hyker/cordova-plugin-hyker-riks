package io.hyker.plugin;

import android.app.Activity;
import io.hyker.riks.box.RiksStorage;
import org.lukhnos.nnio.file.Files;
import org.lukhnos.nnio.file.Paths;

import java.io.*;

/**
 * Created by joakim on 2017-07-10.
 */
public class AndroidRiksStorage implements RiksStorage{

    String workingDir;
    String uid;


    public AndroidRiksStorage(String uid, Activity activity){
        this.workingDir = activity.getApplicationContext().getFilesDir().getAbsolutePath();// + File.separator + "lok";
        this.uid = uid;
    }

    @Override
    public boolean save(Object o, String TAG) throws IOException {
        String path = Paths.get(workingDir, uid + "." + TAG).toString();
        return write(o, path);
    }

    @Override
    public Object load(String TAG) throws IOException, ClassNotFoundException {
        String path = Paths.get(workingDir, uid + "." + TAG).toString();
        return read(path);
    }

    private static byte[] read(String path) throws IOException {
        byte[] data;

        try (FileInputStream fis = new FileInputStream(path)) {
            final int length = (int) Files.size(Paths.get(path));
            data = new byte[length];

            int read = 0;
            while (read != data.length) {
                read += fis.read(data, read, data.length - read);
            }
        }

        return data;
    }
    private static boolean write(Object o, String path) throws IOException {

        File file = new File(path);
        if (!file.exists()) {
            createFile(file);
        }

        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file))) {
            stream.writeObject(o);
            return true;
        }

    }

    private static void createFile(File file) throws IOException {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        file.createNewFile();
    }
}
