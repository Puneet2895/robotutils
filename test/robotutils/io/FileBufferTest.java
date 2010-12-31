/*
 *  The MIT License
 * 
 *  Copyright 2010 pkv.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package robotutils.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import robotutils.io.FileBuffer.Entry;

/**
 * Test harness for FileBuffer, a file-backed Map implementation.
 * 
 * @author pkv
 */
public class FileBufferTest {
    public static File emptyFile;
    public static File testFile;
    public static File tempFile;
    public static final int TEST_FILE_SIZE = 1000;
    public static final String TEST_FILE_BASE = "BARFOO";

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Create empty file.
        emptyFile = File.createTempFile("FileBufferEmptyFile", ".dat");

        // Create temp file.
        tempFile = File.createTempFile("FileBufferTempFile", ".dat");

        // Create test file (with known contents)
        testFile = File.createTempFile("FileBufferTestFile", ".dat");
        
        // Fill in the test file with known data
        FileBuffer fb = new FileBuffer<String>(testFile);
        for (int i = 0; i < TEST_FILE_SIZE; i++) {
             fb.add(TEST_FILE_BASE + i);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // Delete empty file
        if (!emptyFile.delete())
            throw new IOException("Empty file cleanup failed: " + tempFile);

        // Delete temp file
        if (!tempFile.delete())
            throw new IOException("Temp file cleanup failed: " + tempFile);

        // Delete test file
        if (!testFile.delete())
            throw new IOException("Test file cleanup failed: " + testFile);
    }

    /**
     * Test of add method, of class FileBuffer.
     */
    @Test
    public void testAdd() throws Exception {
        System.out.println("add");

        int numTests = 100;
        List<Serializable> contents = new ArrayList<Serializable>(numTests);
        List<Long> addrs = new ArrayList<Long>(numTests);
        
        for (int i = 0; i < numTests; i++) {
            contents.add("FOOBAR" + i);
        }

        try {
            FileBuffer instance = new FileBuffer(tempFile);
            for (Serializable obj : contents) {
                addrs.add(instance.add(obj));
            }

            for (int i = 0; i < addrs.size(); i++) {
                assertEquals( contents.get(i), instance.get(addrs.get(i)) );
            }

        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of addAll method, of class FileBuffer.
     */
    @Test
    public void testAddAll() throws Exception {
        System.out.println("addAll");

        int numTests = 100;
        List<Serializable> contents = new ArrayList<Serializable>(numTests);
        List<Long> addrs = null;

        for (int i = 0; i < numTests; i++) {
            contents.add("FOOBAR" + i);
        }

        try {
            FileBuffer instance = new FileBuffer(tempFile);
            addrs = instance.addAll(contents);

            for (int i = 0; i < addrs.size(); i++) {
                assertEquals( contents.get(i), instance.get(addrs.get(i)) );
            }

        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of isValid method, of class FileBuffer.
     */
    @Test
    public void testIsValid() {
        System.out.println("isValid");

        try {
            FileBuffer instance = new FileBuffer(testFile);

            for (Long uid : (Set<Long>)instance.keySet()) {
                assertTrue( instance.isValid(uid) );
            }

            assertFalse( instance.isValid(-1) );
            assertFalse( instance.isValid(testFile.length() + 10) );
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of readHeader method, of class FileBuffer.
     */
    @Test
    public void testReadHeader() throws Exception {
        System.out.println("readHeader");

        try {
            FileBuffer instance = new FileBuffer(testFile);

            for (Long uid : (Set<Long>)instance.keySet()) {
                Entry header = instance.readHeader(uid);
                assertEquals(uid, header.self);
                assertTrue(instance.isValid(header.prev));
                assertTrue(instance.isValid(header.self));
                assertTrue(header.size > 0);
            }
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of read method, of class FileBuffer.
     */
    @Test
    public void testRead() throws Exception {
        try {
            FileBuffer<String> instance = new FileBuffer(testFile);

            int count = 0;
            for (String foobar : instance.values()) {
                assertEquals(TEST_FILE_BASE + (count++), foobar);
            }

            assertEquals(TEST_FILE_SIZE, count);
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of write method, of class FileBuffer.
     */
    @Test
    public void testWrite() throws Exception {
        System.out.println("write");
        
        try {
            FileBuffer instance = new FileBuffer(tempFile);

            long uid = instance.write("foobarWRITE");
            assertEquals("foobarWRITE", instance.get(uid));

        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of size method, of class FileBuffer.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        
        try {
            FileBuffer<String> instance1 = new FileBuffer(emptyFile);
            assertEquals(0, instance1.size());

            FileBuffer<String> instance2 = new FileBuffer(testFile);
            assertEquals(TEST_FILE_SIZE, instance2.size());
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of entrySet method, of class FileBuffer.
     */
    @Test
    public void testEntrySet() {
        System.out.println("entrySet");

        try {
            FileBuffer<String> instance = new FileBuffer(testFile);
            Set<Map.Entry<Long, String>> entrySet = instance.entrySet();

            assertEquals(TEST_FILE_SIZE, entrySet.size());

            int count = 0;
            for (Map.Entry<Long, String> entry : entrySet) {
                assertTrue(instance.isValid(entry.getKey()));
                assertEquals(TEST_FILE_BASE + count, instance.get(entry.getKey()));
                assertEquals(TEST_FILE_BASE + count, entry.getValue());
                count++;
            }

            assertEquals(TEST_FILE_SIZE, count);
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of isEmpty method, of class FileBuffer.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        
        try {
            FileBuffer<String> instance1 = new FileBuffer(emptyFile);
            assertTrue(instance1.isEmpty());

            FileBuffer<String> instance2 = new FileBuffer(testFile);
            assertFalse(instance2.isEmpty());
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of containsKey method, of class FileBuffer.
     */
    @Test
    public void testContainsKey() {
        System.out.println("containsKey");

        try {
            FileBuffer instance = new FileBuffer(testFile);

            for (Long uid : (Set<Long>)instance.keySet()) {
                assertTrue( instance.containsKey(uid) );
            }

            for (Long uid : (Set<Long>)instance.keySet()) {
                assertFalse( instance.containsKey(uid - 2) );
            }

            for (Long uid : (Set<Long>)instance.keySet()) {
                assertFalse( instance.containsKey(uid + 2) );
            }

        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of containsValue method, of class FileBuffer.
     */
    @Test
    public void testContainsValue() {
        System.out.println("containsValue");

        try {
            FileBuffer<String> instance = new FileBuffer(testFile);

            for (int i = 0; i < TEST_FILE_SIZE; i++) {
                assertTrue(instance.containsValue(TEST_FILE_BASE + i));
            }

            assertFalse(instance.containsValue(TEST_FILE_BASE + "-1"));
            assertFalse(instance.containsValue(TEST_FILE_BASE + (TEST_FILE_SIZE + 1)));
            assertFalse(instance.containsValue("FOOBAR0"));
            
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of keySet method, of class FileBuffer.
     */
    @Test
    public void testKeySet() {
        System.out.println("keySet");
        
        try {
            FileBuffer<String> instance = new FileBuffer(testFile);
            Set<Long> keySet = instance.keySet();

            assertEquals(TEST_FILE_SIZE, keySet.size());
            Long[] keys = keySet.toArray(new Long[0]);

            for (int i = 0; i < TEST_FILE_SIZE; i++) {
                assertTrue(instance.isValid(keys[i]));
                assertEquals(TEST_FILE_BASE + i, instance.get(keys[i]));
            }

        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of values method, of class FileBuffer.
     */
    @Test
    public void testValues() {
        System.out.println("values");

        try {
            FileBuffer<String> instance = new FileBuffer(testFile);
            Collection<String> values = instance.values();

            assertEquals(TEST_FILE_SIZE, values.size());
            
            int count = 0;
            for (String value : values) {
                assertEquals(TEST_FILE_BASE + (count++), value);
            }

            assertEquals(TEST_FILE_SIZE, count);
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of get method, of class FileBuffer.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        
        int numTests = 1000;
        Random rnd = new Random();
        ArrayList<Long> uids = new ArrayList<Long>(numTests);
        ArrayList<String> contents = new ArrayList<String>(numTests);

        try {
            for (int i = 0; i < numTests; i++) {
                FileBuffer<String> instance = new FileBuffer(tempFile);
                String randStr = "FOO" + rnd.nextLong() + "BAR";

                contents.add(randStr);
                uids.add(instance.add(randStr));

                int index = rnd.nextInt(contents.size());
                assertEquals(contents.get(index), instance.get(uids.get(index)));
                assertNull(instance.get(uids.get(index) + 3));
                assertNull(instance.get(uids.get(index) - 3));
            }
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
    }

    /**
     * Test of put method, of class FileBuffer.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        Long k = 23111L;
        Serializable v = "foobar";
        
        try {
            FileBuffer instance = new FileBuffer(tempFile);
            instance.put(k, v);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Threw " + ex);
            return;
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }

        fail("Exception was not thrown by put().");
    }

    /**
     * Test of putAll method, of class FileBuffer.
     */
    @Test
    public void testPutAll() {
        System.out.println("putAll");
        Map<Long, Object> map = new HashMap<Long, Object>();
        
        try {
            FileBuffer instance = new FileBuffer(tempFile);
            instance.putAll(map);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Threw " + ex);
            return;
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }
        
        fail("Exception was not thrown by putAll().");
    }

    /**
     * Test of remove method, of class FileBuffer.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        Serializable o = "foobar";

        try {
            FileBuffer instance = new FileBuffer(tempFile);
            instance.remove(o);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Threw " + ex);
            return;
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }

        fail("Exception was not thrown by remove().");
    }

    /**
     * Test of clear method, of class FileBuffer.
     */
    @Test
    public void testClear() {
        System.out.println("clear");

        try {
            FileBuffer instance = new FileBuffer(tempFile);
            instance.clear();
        } catch (UnsupportedOperationException ex) {
            System.out.println("Threw " + ex);
            return;
        } catch (FileNotFoundException ex) {
            fail("Did not find data file: " + ex);
        }

        fail("Exception was not thrown by clear().");
    }

}