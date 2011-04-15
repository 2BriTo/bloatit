package com.bloatit.data.queries;

import java.util.Locale;

import com.bloatit.data.DaoDescription;
import com.bloatit.data.DaoFeature;
import com.bloatit.data.DaoIdentifiable;
import com.bloatit.data.DataTestUnit;

public class DaoIdentifiableListFactoryTest extends DataTestUnit {

    public void testDaoIdentifiableListFactory() {

        final DaoFeature feature = DaoFeature.createAndPersist(yo,
                                                               null,
                                                               DaoDescription.createAndPersist(yo,
                                                                                               null,
                                                                                               new Locale("fr"),
                                                                                               "Ma super demande !",
                                                                                               "Ceci est la descption de ma demande :) "),
                                                               project);

        final DaoIdentifiableQuery<DaoIdentifiable> factory = new DaoIdentifiableQuery<DaoIdentifiable>();
        factory.idEquals(feature.getId());
        assertEquals(feature, factory.uniqueResult());
    }

    public void testCreateCollection() {
        final DaoIdentifiableQuery<DaoIdentifiable> factory = new DaoIdentifiableQuery<DaoIdentifiable>();

        assertTrue(factory.createCollection().size() != 0);
    }

}
