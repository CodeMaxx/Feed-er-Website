# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2016-10-28 20:09
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('website', '0010_auto_20161028_2004'),
    ]

    operations = [
        migrations.AlterModelOptions(
            name='feedback',
            options={'ordering': ['-deadline']},
        ),
        migrations.RemoveField(
            model_name='feedback',
            name='added_on',
        ),
        migrations.AlterField(
            model_name='feedback',
            name='deadline',
            field=models.DateTimeField(),
        ),
    ]