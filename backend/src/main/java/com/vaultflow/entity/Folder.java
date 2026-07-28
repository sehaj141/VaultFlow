package com.vaultflow.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "folders", indexes = {
    @Index(name = "idx_folders_user_parent", columnList = "user_id, parent_id"),
    @Index(name = "idx_folders_path", columnList = "path")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_parent_folder_name", columnNames = {"user_id", "parent_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Folder parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Folder> subfolders = new ArrayList<>();

    @Column(nullable = false, length = 1000)
    private String path; // e.g. "/root-uuid/parent-uuid/folder-uuid"

    @Column(nullable = false)
    @Builder.Default
    private Integer depth = 0;

    @Column(name = "is_trashed", nullable = false)
    @Builder.Default
    private Boolean isTrashed = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
