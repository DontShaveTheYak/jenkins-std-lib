package com.dsty

public class SharedLibraryConstants {
	public static final String DEPLOY_COMMAND = """
	docker-compose pull && \
	docker-compose down && \
	docker-compose rm -f && \
	docker-compose up -d --force-recreate"""
}