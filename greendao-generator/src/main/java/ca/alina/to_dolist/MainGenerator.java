package ca.alina.to_dolist;

import org.greenrobot.greendao.generator.DaoGenerator;
import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;
import org.greenrobot.greendao.generator.ToMany;

public class MainGenerator
{
    public static void main(String[] args)
            throws Exception
    {
        final Schema       schema;
        final Entity       taskEntity;
        final Entity       notifEntity;
        final DaoGenerator generator;

        schema = new Schema(2, "ca.alina.to_dolist.database.schema");
        taskEntity = schema.addEntity("Task");

        taskEntity.addIdProperty();
        taskEntity.addStringProperty("name").notNull();
        taskEntity.addStringProperty("notes").notNull();
        taskEntity.addDateProperty("startTime").notNull();
        taskEntity.addDateProperty("endTime");
        taskEntity.addDateProperty("dueDate");
        taskEntity.addDateProperty("markedDoneTime");
        taskEntity.addBooleanProperty("isDone").notNull();
        taskEntity.addBooleanProperty("isHidden").notNull();
        taskEntity.addBooleanProperty("isAlarm").notNull();
        taskEntity.addBooleanProperty("isRecurring").notNull();
        taskEntity.addStringProperty("recurrenceRule");

        notifEntity = schema.addEntity("Notif");

        notifEntity.addIdProperty();
        Property taskId = notifEntity.addLongProperty("taskId").notNull().getProperty();

        ToMany taskToNotifs = taskEntity.addToMany(notifEntity, taskId);
        taskToNotifs.setName("notifs");

        generator = new DaoGenerator();
        generator.generateAll(schema, "./app/src/main/java");
    }
}
