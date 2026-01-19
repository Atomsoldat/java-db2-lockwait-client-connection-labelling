FROM icr.io/db2_community/db2

COPY entrypoint-override-db2.sh /usr/local/bin/entrypoint-override-db2.sh

ENTRYPOINT ["/usr/local/bin/entrypoint-override-db2.sh"]
