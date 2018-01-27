ALTER TABLE queue_request ADD CONSTRAINT queue_request_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE queue_request ADD CONSTRAINT queue_request_space_id FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE CASCADE;

ALTER TABLE queue_reserved ADD CONSTRAINT queue_reserved_space_id FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE CASCADE;
ALTER TABLE queue_reserved ADD CONSTRAINT queue_reserved_queue_id FOREIGN KEY (queue_id) REFERENCES queues(id) ON DELETE CASCADE;

ALTER TABLE temp_node_changes ADD CONSTRAINT temp_node_changes_space_id FOREIGN KEY (space_id) REFERENCES spaces(id) ON DELETE CASCADE;
