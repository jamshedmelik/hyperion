package com.krux.hyperion.examples

import org.scalatest.WordSpec
import org.json4s.jackson.JsonMethods._
import org.json4s.JsonDSL._
import org.json4s._
import com.krux.hyperion.DataPipelineDef._


class ExampleRedshiftLoadSpec extends WordSpec {

  "ExampleRedshiftLoad" should {

    "produce correct pipeline JSON" in {

      val pipelineJson: JValue = new ExampleRedshiftLoad
      val objectsField = pipelineJson.children(0).children.sortBy(o => (o \ "id").toString)

      // have the correct number of objects
      assert(objectsField.size === 8)

      val default = objectsField(0)
      val defaultShouldBe =
        ("id" -> "Default") ~
        ("name" -> "Default") ~
        ("scheduleType" -> "cron") ~
        ("failureAndRerunMode" -> "CASCADE") ~
        ("pipelineLogUri" -> "s3://your-bucket/datapipeline-logs/") ~
        ("role" -> "DataPipelineDefaultRole") ~
        ("resourceRole" -> "DataPipelineDefaultResourceRole") ~
        ("schedule" -> ("ref" -> "PipelineSchedule"))
      assert(default === defaultShouldBe)

      val ec2 = objectsField(1)
      val ec2ShouldBe =
        ("id" -> "Ec2Resource") ~
        ("name" -> "Ec2Resource") ~
        ("terminateAfter" -> "8 hours") ~
        ("imageId" -> "ami-b0682cd8") ~
        ("instanceType" -> "m1.small") ~
        ("region" -> "us-east-1") ~
        ("securityGroups" -> Seq("your-security-group")) ~
        ("associatePublicIpAddress" -> "false") ~
        ("keyPair" -> "your-aws-key-pair") ~
        ("type" -> "Ec2Resource")
      assert(ec2 === ec2ShouldBe)

      val pipelineSchedule = objectsField(2)
      val pipelineScheduleShouldBe =
        ("id" -> "PipelineSchedule") ~
        ("name" -> "PipelineSchedule") ~
        ("period" -> "1 hours") ~
        ("startAt" -> "FIRST_ACTIVATION_DATE_TIME") ~
        ("type" -> "Schedule")
      assert(pipelineSchedule === pipelineScheduleShouldBe)

      val s3DataNode = objectsField(3)
      val s3DataNodeId: String = (s3DataNode \ "id").values.toString
      assert(s3DataNodeId.startsWith("S3DataNode_"))
      val s3DataNodeShouldBe =
        ("id" -> s3DataNodeId) ~
        ("name" -> s3DataNodeId) ~
        ("dataFormat" -> ("ref" -> "tsv")) ~
        ("directoryPath" -> "s3://testing/testtab/") ~
        ("type" -> "S3DataNode")
      assert(s3DataNode === s3DataNodeShouldBe)

      val mockRedshift = objectsField(4)
      val mockRedshiftShouldBe =
        ("id" -> "_MockRedshift") ~
        ("name" -> "_MockRedshift") ~
        ("clusterId" -> "mock-redshift") ~
        ("databaseName" -> "mock_db") ~
        ("*password" -> "mockpass") ~
        ("username" -> "mockuser") ~
        ("type" -> "RedshiftDatabase")
      assert(mockRedshift === mockRedshiftShouldBe)

      val copy = objectsField(5)
      val copyShouldBe =
        ("id" -> "copy") ~
        ("name" -> "copy") ~
        ("input" -> ("ref" -> s3DataNodeId)) ~
        ("insertMode" -> "OVERWRITE_EXISTING") ~
        ("output" -> ("ref" -> "destTable")) ~
        ("runsOn" -> ("ref" -> "Ec2Resource")) ~
        ("type" -> "RedshiftCopyActivity")
      assert(copy === copyShouldBe)

      val destTable = objectsField(6)
      val destTableShouldBe =
        ("id" -> "destTable") ~
        ("name" -> "destTable") ~
        ("database" -> ("ref" -> "_MockRedshift")) ~
        ("schemaName" -> "kexin") ~
        ("tableName" -> "monthly_campaign_frequency_distribution") ~
        ("primaryKeys" -> List("publisher_id", "campaign_id", "month")) ~
        ("type" -> "RedshiftDataNode")
      assert(destTable === destTableShouldBe)

      val tsv = objectsField(7)
      val tsvShouldBe =
        ("id" -> "tsv") ~
        ("name" -> "tsv") ~
        ("type" -> "TSV")
      assert(tsv === tsvShouldBe)

    }
  }

}