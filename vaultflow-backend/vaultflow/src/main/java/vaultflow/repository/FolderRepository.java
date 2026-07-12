package vaultflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vaultflow.entity.Folder;
import vaultflow.entity.User;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByOwner(User owner);

    List<Folder> findByParent(Folder parent);

    List<Folder> findByOwnerAndParent(User owner, Folder parent);

}