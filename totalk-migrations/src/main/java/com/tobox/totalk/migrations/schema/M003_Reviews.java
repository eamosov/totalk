package com.tobox.totalk.migrations.schema;

import io.smartcat.migration.SchemaMigration;
import io.smartcat.migration.exceptions.MigrationException;

public class M003_Reviews extends SchemaMigration {

	public M003_Reviews() {
		super(3);
	}

	@Override
	public String getDescription() {
		return "Delete reviews.entity_type";
	}

	@Override
	public void execute() throws MigrationException {
		session.execute("ALTER TABLE reviews DROP entity_type");
	}

}
