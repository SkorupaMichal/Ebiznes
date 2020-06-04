package models.auth.daos

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile


trait DAOSlick extends DBTableDefinitions with HasDatabaseConfigProvider[JdbcProfile]
