FROM postgres:17

# Copy the initialization script
COPY ./init-databases.sh /docker-entrypoint-initdb.d/01-init-databases.sh

# Convert Windows line endings (CRLF) to Unix (LF) and make executable
# This works on both Windows and Linux Docker without needing dos2unix
RUN sed -i 's/\r$//' /docker-entrypoint-initdb.d/01-init-databases.sh && \
    chmod +x /docker-entrypoint-initdb.d/01-init-databases.sh

# Expose PostgreSQL port
EXPOSE 5432

# The postgres image already has an ENTRYPOINT, so we don't need to specify it
