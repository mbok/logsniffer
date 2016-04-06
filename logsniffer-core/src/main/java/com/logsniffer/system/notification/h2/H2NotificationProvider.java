package com.logsniffer.system.notification.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.logsniffer.system.notification.Notification;
import com.logsniffer.system.notification.Notification.Level;
import com.logsniffer.system.notification.Notification.Type;
import com.logsniffer.system.notification.NotificationProvider;
import com.logsniffer.util.PageableResult;

/**
 * H2 implementation of {@link NotificationProvider}.
 * 
 * @author mbok
 *
 */
@Component
public class H2NotificationProvider implements NotificationProvider {
	private static final String TABLE_NAME = "SYSTEM_NOTIFICATIONS";
	private static final String TABLE_NAME_ACK = "SYSTEM_NOTIFICATIONS_ACKS";
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private class NotificationCreator implements PreparedStatementCreator {
		private static final String SQL_SET = TABLE_NAME
				+ " SET ID=?, NTYPE=?, TITLE=?, MESSAGE=?, LEVEL=?, EXPIRATION=?, CREATION=?";
		private static final String SQL_INSERT = "INSERT INTO " + SQL_SET;
		private final Notification notification;

		private NotificationCreator(final Notification notification) {
			this.notification = notification;
		}

		@Override
		public PreparedStatement createPreparedStatement(final Connection con) throws SQLException {
			final PreparedStatement ps = con.prepareStatement(SQL_INSERT);
			int c = 1;
			notification.setCreationDate(new java.util.Date());
			ps.setString(c++, notification.getId());
			ps.setInt(c++, notification.getType().ordinal());
			ps.setString(c++, notification.getTitle());
			ps.setString(c++, notification.getMessage());
			ps.setInt(c++, notification.getLevel().ordinal());
			ps.setTimestamp(c++, notification.getExpirationDate() != null
					? new Timestamp(notification.getExpirationDate().getTime()) : null);
			ps.setTimestamp(c++, new Timestamp(notification.getCreationDate().getTime()));
			return ps;
		}

	}

	private class NotificationRowMapper implements RowMapper<Notification> {
		@Override
		public Notification mapRow(final ResultSet rs, final int rowNum) throws SQLException {
			final Notification n = new Notification();
			n.setId(rs.getString("ID"));
			n.setType(mapType(rs.getInt("NTYPE")));
			n.setTitle(rs.getString("TITLE"));
			n.setMessage(rs.getString("MESSAGE"));
			n.setLevel(mapLevel(rs.getInt("LEVEL")));
			n.setExpirationDate(rs.getTimestamp("EXPIRATION"));
			n.setCreationDate(rs.getTimestamp("CREATION"));
			return n;
		}

		private Type mapType(final int ord) {
			if (ord >= 0 && ord < Type.values().length) {
				return Type.values()[ord];
			}
			logger.error("Failed to map type with index: {}", ord);
			return Type.MESSAGE;
		}

		private Level mapLevel(final int ord) {
			if (ord >= 0 && ord < Level.values().length) {
				return Level.values()[ord];
			}
			logger.error("Failed to map level with index: {}", ord);
			return Level.INFO;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean store(final Notification n, final boolean override) {
		if (jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE ID=?", Integer.class, n.getId())
				.intValue() == 0) {
			jdbcTemplate.update(new NotificationCreator(n));
			logger.debug("Stored notification: {}", n);
			return true;
		} else if (override) {
			delete(n.getId());
			jdbcTemplate.update(new NotificationCreator(n));
			logger.debug("Overridden notification: {}", n);
			return true;
		} else if (n.getExpirationDate() != null
				&& jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE ID=? AND EXPIRATION = ?",
						Integer.class, n.getId(), n.getExpirationDate()).intValue() == 0) {
			jdbcTemplate.update("UPDATE " + TABLE_NAME + " SET EXPIRATION = ? WHERE ID=?", n.getExpirationDate(),
					n.getId());
			logger.debug("Updated expiration date of notification: {}", n);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public PageableResult<Notification> getNotifications(final String userToken, final int from, final int limit) {
		final PageableResult<Notification> n = new PageableResult<>();
		n.setTotalCount(getSummary(userToken).getCount());
		n.setItems(jdbcTemplate.query(
				"SELECT a.ID, a.NTYPE, a.TITLE, a.MESSAGE, a.LEVEL, a.EXPIRATION, a.CREATION FROM " + TABLE_NAME
						+ " a LEFT JOIN " + TABLE_NAME_ACK
						+ " b ON (a.ID = b.ID AND b.USER=?) WHERE a.NTYPE=? OR (b.USER IS NULL AND (a.EXPIRATION IS NULL OR a.EXPIRATION >= ?))"
						+ " ORDER BY a.LEVEL DESC, a.CREATION DESC",
				new Object[] { userToken, Type.MESSAGE.ordinal(), new Date() }, new NotificationRowMapper()));
		return n;
	}

	@Override
	public NotificationSummary getSummary(final String userToken) {
		final long start = System.currentTimeMillis();
		final Map<String, Object> r = jdbcTemplate.queryForMap(
				"SELECT COUNT(*) as c, MAX(a.LEVEL) as m FROM " + TABLE_NAME + " a LEFT JOIN " + TABLE_NAME_ACK
						+ " b ON (a.ID = b.ID AND b.USER=?) WHERE NTYPE=? OR (b.USER IS NULL AND (a.EXPIRATION IS NULL OR a.EXPIRATION >= ?))",
				userToken, Type.MESSAGE.ordinal(), new Date());
		Level worst = null;
		if (r.get("m") != null) {
			final int worstOrd = ((Number) r.get("m")).intValue();
			if (worstOrd >= 0 && worstOrd < Level.values().length) {
				worst = Level.values()[worstOrd];
			} else {
				logger.error("Failed to read worst level from database, ordinal number is invalid: {}", worstOrd);
			}
		}
		int c = 0;
		if (r.get("c") != null) {
			c = ((Number) r.get("c")).intValue();
		}
		logger.debug("Took {}ms for caluclating notification summary", System.currentTimeMillis() - start);
		return new NotificationSummary(c, worst);
	}

	@Override
	public void acknowledge(final String notificationId, final String userToken) {
		jdbcTemplate.update("INSERT INTO " + TABLE_NAME_ACK + " SET ID = ?, USER = ?", notificationId, userToken);
	}

	@Override
	@Transactional
	public void delete(final String notificationId) {
		jdbcTemplate.update("DELETE FROM " + TABLE_NAME_ACK + " WHERE ID = ?", notificationId);
		jdbcTemplate.update("DELETE FROM " + TABLE_NAME + " WHERE ID = ?", notificationId);
	}

}
