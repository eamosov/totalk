package com.tobox.totalk.migrations.schema;

import io.smartcat.migration.SchemaMigration;
import io.smartcat.migration.exceptions.MigrationException;

public class M002_Reviews extends SchemaMigration {

	public M002_Reviews() {
		super(2);
	}

	@Override
	public String getDescription() {
		return "Add reviews and review_comments tables";
	}

	@Override
	public void execute() throws MigrationException {
		session.execute("CREATE TABLE reviews (id uuid PRIMARY KEY, type int, deleted boolean, deleted_at timestamp, entity_type int, entity_id uuid, category_id int, country int, creator_id uuid, created_at timestamp, updated_at timestamp,  comments_allowed boolean, title text, body text)");
		session.execute("CREATE TABLE review_comments (id uuid PRIMARY KEY, review_id uuid, deleted boolean, deleted_at timestamp, creator_id uuid, created_at timestamp, updated_at timestamp,  body text)");
	}

}
