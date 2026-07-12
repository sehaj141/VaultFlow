package vaultflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vaultflow.entity.FileEntity;
import vaultflow.entity.Folder;
import vaultflow.entity.User;

public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByOwner(User owner);

    List<FileEntity> findByFolder(Folder folder);

    List<FileEntity> findByOwnerAndFolder(User owner, Folder folder);

}