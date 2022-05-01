package com.celonis.challenge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "tasks")
@Data
public class ProjectGenerationTask {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String name;

    private Date creationDate;

    private int x;

    private int y;

    @JsonIgnore
    private String storageLocation;

    private TaskStatus taskStatus = TaskStatus.CREATED;

}
