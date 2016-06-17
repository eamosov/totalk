package com.tobox.totalk.migrations.schema;

import io.smartcat.migration.SchemaMigration;
import io.smartcat.migration.exceptions.MigrationException;

public class M001_Initial extends SchemaMigration {

	public M001_Initial() {
		super(1);
	}

	@Override
	public String getDescription() {
		return "M001_Initial";
	}

	@Override
	public void execute() throws MigrationException {
		
	}

}
