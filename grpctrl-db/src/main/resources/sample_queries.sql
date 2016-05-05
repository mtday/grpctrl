
-- How to recursively find all groups for a specific group id
WITH RECURSIVE all_groups AS (
    SELECT parent_id, group_id, group_name, 1 AS depth
        FROM groups WHERE account_id = 1 AND group_id = 1
    UNION ALL
    SELECT g.parent_id, g.group_id, g.group_name, depth + 1
        FROM groups g JOIN all_groups a ON
            (g.account_id = 1 AND g.parent_id = a.group_id AND depth < 5)
)
SELECT parent_id, group_id, group_name, depth FROM all_groups LIMIT 100;


-- How to determine the depth of a group given the group id.
WITH RECURSIVE parents AS (
    SELECT parent_id, group_id, group_name, 1 AS depth
        FROM groups WHERE account_id = 1 AND group_id = 5555
    UNION ALL
    SELECT g.parent_id, g.group_id, g.group_name, depth + 1
        FROM groups g JOIN parents p ON
            (g.account_id = 1 AND g.group_id = p.parent_id)
)
SELECT MAX(depth) AS depth FROM parents;

