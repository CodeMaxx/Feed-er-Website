.PHONY: help run mig mkmg

help:
	@echo
	@echo "make run : runserver"
	@echo "make mig: migrate"
	@echo "make mkmg: makemigrations"
	@echo "feel free to add new features and update the help section."
	@echo
run:
	@python manage.py runserver

mig:
	@python manage.py migrate

mkmg:
	@python manage.py makemigrations

