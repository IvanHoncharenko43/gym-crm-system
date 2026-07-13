package org.example.config;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DataInitializationProcessorTest {

    @InjectMocks
    private DataInitializationProcessor processor;

    @Test
    void init_ParseData_FileIsValid() throws Exception {
        setFilePath("valid-test-data.json");
        processor.init();
        JsonNode rootNode = getRootNode();
        assertNotNull(rootNode);
        assertTrue(rootNode.has("test"));
    }

    @Test
    void init_LeaveRootNodeNull_FileDoesNotExist() throws Exception {
        setFilePath("non-existent-file.json");
        processor.init();
        assertNull(getRootNode());
    }

    @Test
    void init_HandleExceptionAndLeaveRootNodeNull_JsonIsInvalid() throws Exception {
        setFilePath("invalid-test-data.json");
        processor.init();
        assertNull(getRootNode());
    }

    private void setFilePath(String path) throws Exception {
        Field field = DataInitializationProcessor.class.getDeclaredField("filePath");
        field.setAccessible(true);
        field.set(processor, path);
    }

    private JsonNode getRootNode() throws Exception {
        Field field = DataInitializationProcessor.class.getDeclaredField("rootNode");
        field.setAccessible(true);
        return (JsonNode) field.get(processor);
    }
}
