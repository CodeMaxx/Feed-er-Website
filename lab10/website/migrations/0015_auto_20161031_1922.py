# -*- coding: utf-8 -*-
# Generated by Django 1.10.2 on 2016-10-31 19:22
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('website', '0014_member_token'),
    ]

    operations = [
        migrations.AlterField(
            model_name='course',
            name='members',
            field=models.ManyToManyField(blank=True, to='website.Member'),
        ),
    ]
