package com.bloatit.model.data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.annotations.Entity;

import com.bloatit.common.Log;
import com.bloatit.model.data.util.NonOptionalParameterException;
import com.bloatit.model.data.util.SessionManager;

@Entity
public class DaoFile extends DaoUserContent {

    public enum FileType {
        TEXT, HTML, TEX, PDF, ODT, DOC, BMP, JPG, PNG, SVG,
    }

    @Basic(optional = false)
    private final String filename;

    @Basic(optional = false)
    private final String directory;

    @Basic(optional = false)
    private final int size;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Basic(optional = false)
    @Enumerated
    private final FileType type;

    @ManyToOne(optional = true)
    private final DaoUserContent relatedContent;

    public static DaoFile createAndPersist(DaoMember member, DaoUserContent relatedContent, String filename, String directory, FileType type, int size) {
        final Session session = SessionManager.getSessionFactory().getCurrentSession();
        final DaoFile file = new DaoFile(member, relatedContent, filename, directory, type, size);
        try {
            session.save(file);
        } catch (final HibernateException e) {
            session.getTransaction().rollback();
            Log.data().error(e);
            session.beginTransaction();
            throw e;
        }
        return file;
    }

    /**
     * @param member is the author (the one who uploaded the file)
     * @param relatedContent can be null. It is the content with which this file has been
     *        uploaded.
     * @param filename is the name of the file (with its extension, but without its whole
     *        folder path)
     * @param directory is the path of the directory where the file is.
     * @param type is the type of the file (found using its extension or mimetype)
     * @param size is the size of the file.
     */
    private DaoFile(DaoMember member, DaoUserContent relatedContent, String filename, String directory, FileType type, int size) {
        super(member);
        if (filename == null || directory == null || type == null || filename.isEmpty() || directory.isEmpty()) {
            throw new NonOptionalParameterException();
        }
        this.size = size;
        this.filename = filename;
        this.directory = directory;
        this.type = type;
        this.shortDescription = null;
        this.relatedContent = relatedContent;
        // At the end to make sure the assignment are done.
        // It works only if equal is final !!
        if (this.equals(relatedContent)) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @param shortDescription the shortDescription to set
     */
    public final void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    /**
     * @return the the directory + filename.
     */
    public final String getFilePath() {
        return directory + filename;
    }

    /**
     * @return the shortDescription
     */
    public final String getShortDescription() {
        return shortDescription;
    }

    /**
     * @return the filename
     */
    public final String getFilename() {
        return filename;
    }

    /**
     * @return the directory
     */
    public final String getFolder() {
        return directory;
    }

    /**
     * @return the size
     */
    public final int getSize() {
        return size;
    }

    public FileType getType() {
        return type;
    }

    public DaoUserContent getRelatedContent() {
        return relatedContent;
    }

}
